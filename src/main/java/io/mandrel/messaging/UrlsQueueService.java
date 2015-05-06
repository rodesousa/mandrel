package io.mandrel.messaging;

import io.mandrel.common.data.Spider;
import io.mandrel.data.content.selector.Selector.Instance;
import io.mandrel.data.extract.ExtractorService;
import io.mandrel.http.Metadata;
import io.mandrel.http.Requester;
import io.mandrel.stats.Stats;
import io.mandrel.stats.StatsService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Component
@Slf4j
public class UrlsQueueService {

	private final QueueService queueService;

	private final Requester requester;

	private final StatsService statsService;

	private final ExtractorService extractorService;

	@Inject
	public UrlsQueueService(QueueService queueService, Requester requester, ExtractorService extractorService, StatsService statsService) {
		this.queueService = queueService;
		this.requester = requester;
		this.extractorService = extractorService;
		this.statsService = statsService;
	}

	public void add(long spiderId, Set<String> urls) {
		queueService.add("urls-" + spiderId, urls);
	}

	public void registrer(Spider spider) {
		log.debug("Registering spider {} ({})", spider.getName(), spider.getId());

		Stats stats = statsService.get(spider.getId());
		queueService.<String> registrer("urls-" + spider.getId(), url -> {
			long maxPages = spider.getClient().getPoliteness().getMaxPages();
			if (maxPages > 0 && (stats.getNbPages() + stats.getNbPendingPages()) > maxPages) {
				log.debug("Max pages reached for {} ({})", spider.getName(), spider.getId());
				return true;
			}
			doRequest(spider, url, stats);
			return false;
		});
	}

	private void doRequest(Spider spider, String url, Stats stats) {
		try {
			StopWatch watch = new StopWatch();
			watch.start();

			// Mark as pending
			queueService.markAsPending("pendings-" + spider.getId(), url, Boolean.TRUE);
			stats.incNbPendingPages();

			requester.get(url, spider, webPage -> {

				watch.stop();

				log.debug("> Start parsing data for {}", url);

				Metadata metadata = webPage.getMetadata();
				metadata.setTimeToFetch(watch.getTotalTimeMillis());

				stats.incNbPages();
				stats.incPageForStatus(metadata.getStatusCode());
				stats.incTotalTimeToFetch(watch.getLastTaskTimeMillis());
				stats.incTotalSize(webPage.getBody().length);

				Map<String, Instance<?>> cachedSelectors = new HashMap<>();
				if (spider.getExtractors() != null && spider.getExtractors().getPages() != null) {
					log.trace(">  - Extracting documents for {}...", url);
					spider.getExtractors().getPages().forEach(ex -> extractorService.extractThenFormatThenStore(spider.getId(), cachedSelectors, webPage, ex));
					log.trace(">  - Extracting documents for {} done!", url);
				}

				if (spider.getExtractors().getOutlinks() != null) {
					log.trace(">  - Extracting outlinks for {}...", url);
					spider.getExtractors().getOutlinks().forEach(ol -> {
						Set<String> allFilteredOutlinks = extractorService.extractAndFilterOutlinks(spider, url, cachedSelectors, webPage, ol).getRight();

						metadata.setOutlinks(allFilteredOutlinks);

						// Respect politeness for this
						// spider
						// TODO

							// Add outlinks to queue if they
							// are not already present
							allFilteredOutlinks = queueService.deduplicate("urls-" + spider.getId(), allFilteredOutlinks);

							allFilteredOutlinks = queueService.filterPendings("pendings-" + spider.getId(), allFilteredOutlinks);
							add(spider.getId(), allFilteredOutlinks);
						});
					log.trace(">  - Extracting outlinks done for {}!", url);
				}

				if (spider.getStores().getPageStore() != null) {
					log.trace(">  - Storing page {}...", url);
					spider.getStores().getPageStore().addPage(spider.getId(), webPage.getUrl().toString(), webPage);
					log.trace(">  - Storing page {} done!", url);
				}

				log.trace(">  - Storing metadata for {}...", url);
				spider.getStores().getPageMetadataStore().addMetadata(spider.getId(), webPage.getUrl().toString(), metadata);
				log.trace(">  - Storing metadata for {} done!", url);

				queueService.removePending("pendings-" + spider.getId(), url);
				stats.decNbPendingPages();

				log.debug("> End parsing data for {}", url);
			}, t -> {
				// Well...
					queueService.removePending("pendings-" + spider.getId(), url);
					stats.decNbPendingPages();
				});
		} catch (Exception e) {
			log.debug("Can not fetch url {} due to {}", new Object[] { url, e.toString() }, e);
		}
	}

}
