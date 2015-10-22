package io.mandrel.common.thrift;

import io.mandrel.common.data.Spider;
import io.mandrel.controller.ControllerContainers;
import io.mandrel.controller.thrift.ActiveFrontier;
import io.mandrel.controller.thrift.ActiveTask;
import io.mandrel.controller.thrift.Controller;
import io.mandrel.controller.thrift.Heartbeat;
import io.mandrel.frontier.FrontierContainer;
import io.mandrel.frontier.FrontierContainers;
import io.mandrel.frontier.Frontiers;
import io.mandrel.frontier.thrift.Frontier;
import io.mandrel.frontier.thrift.Result;
import io.mandrel.frontier.thrift.Uri;
import io.mandrel.monitor.Infos;
import io.mandrel.monitor.SigarService;
import io.mandrel.worker.WorkerContainer;
import io.mandrel.worker.WorkerContainers;
import io.mandrel.worker.thrift.Worker;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Set;

import javax.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;

import org.apache.thrift.TException;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import com.google.inject.Inject;
import com.netflix.servo.util.Throwables;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TEndpoint {

	private final ObjectMapper mapper;

	@PostConstruct
	public void run() {

		// TODO to be externalized
		boolean worker = true;
		boolean frontier = true;
		boolean controller = true;

		TMultiplexedProcessor processor = new TMultiplexedProcessor();
		if (worker) {
			processor.registerProcessor("Worker", new Worker.Processor<Worker.Iface>(new WorkerHandler(mapper)));
		}
		if (frontier) {
			processor.registerProcessor("Frontier", new Frontier.Processor<Frontier.Iface>(new FrontierHandler(mapper)));
		}
		if (controller) {
			processor.registerProcessor("Controller", new Controller.Processor<Controller.Iface>(new ControllerHandler(mapper)));
		}
		processor.registerProcessor("Cluster", new Cluster.Processor<Cluster.Iface>(new ClusterHandler(null)));

		TNonblockingServerTransport transport;
		try {
			transport = new TNonblockingServerSocket(9090);
		} catch (TTransportException e) {
			throw Throwables.propagate(e);
		}

		TThreadedSelectorServer.Args args = new TThreadedSelectorServer.Args(transport);
		args.transportFactory(new TFramedTransport.Factory());
		args.protocolFactory(new TBinaryProtocol.Factory());
		args.processor(processor);
		args.selectorThreads(4);
		args.workerThreads(32);
		TServer server = new TThreadedSelectorServer(args);

		server.serve();

		// Arrange to stop the server at shutdown
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				server.stop();
			}
		});
	}

	@RequiredArgsConstructor
	public static class ClusterHandler implements Cluster.Iface {

		private final SigarService sigarService;

		@Override
		public Node get() throws TException {
			Infos infos = sigarService.infos();
			return new Node().setId(infos.getFqdn()).setInfos(infos.getHostname());
		}
	}

	@RequiredArgsConstructor
	public static class ControllerHandler implements Controller.Iface {

		private final ObjectMapper mapper;

		@Override
		public void pulse(Heartbeat beat) throws TException {
			beat.getInfo();
		}

		@Override
		public void start(long id) throws TException {
			ControllerContainers.get(id).ifPresent(c -> c.start());
		}

		@Override
		public void pause(long id) throws TException {
			ControllerContainers.get(id).ifPresent(c -> c.pause());
		}

		@Override
		public void kill(long id) throws TException {
			ControllerContainers.get(id).ifPresent(c -> c.kill());
		}

		@Override
		public Set<ActiveTask> syncTasks() throws TException {
			// TODO
			return null;
		}

		@Override
		public Set<ActiveFrontier> syncFrontiers() throws TException {
			// TODO
			return null;
		}
	}

	@RequiredArgsConstructor
	public static class FrontierHandler implements Frontier.Iface {

		private final ObjectMapper mapper;

		@Override
		public void create(ByteBuffer definition) throws TException {
			Spider spider = null;
			try {
				spider = mapper.readValue(new ByteBufferBackedInputStream(definition), Spider.class);
			} catch (IOException e) {
				e.printStackTrace();
			}

			// TODO
			FrontierContainer container = new FrontierContainer(spider, null);
			container.register();
		}

		@Override
		public void start(long id) throws TException {
			FrontierContainers.get(id).ifPresent(c -> c.start());
		}

		@Override
		public void pause(long id) throws TException {
			FrontierContainers.get(id).ifPresent(c -> c.pause());
		}

		@Override
		public void kill(long id) throws TException {
			FrontierContainers.get(id).ifPresent(c -> c.kill());
		}

		@Override
		public Uri take(long id) throws TException {
			// TODO null???
			URI pool = Frontiers.get(id).map(f -> f.pool()).orElse(null);
			return new Uri().setReference(pool.toString());
		}

		@Override
		public void schedule(long id, Uri uri) throws TException {
			Frontiers.get(id).ifPresent(f -> f.schedule(URI.create(uri.getReference())));
		}

		@Override
		public void scheduleM(long id, Set<Uri> uris) throws TException {
			Frontiers.get(id).ifPresent(f -> uris.forEach(uri -> f.schedule(URI.create(uri.getReference()))));
		}

		@Override
		public void finished(long id, Result result) throws TException {
			// TODO
		}

		@Override
		public void deleted(long id, Uri uri) throws TException {
			Frontiers.get(id).ifPresent(f -> f.delete(URI.create(uri.getReference())));
		}
	}

	@RequiredArgsConstructor
	public static class WorkerHandler implements Worker.Iface {

		private final ObjectMapper mapper;

		@Override
		public void create(ByteBuffer definition) throws TException {
			Spider spider = null;
			try {
				spider = mapper.readValue(new ByteBufferBackedInputStream(definition), Spider.class);
			} catch (IOException e) {
				e.printStackTrace();
			}

			// TODO
			WorkerContainer container = new WorkerContainer(null, null, spider, null, null);
			container.register();
		}

		@Override
		public void start(long id) throws TException {
			WorkerContainers.get(id).ifPresent(c -> c.start());
		}

		@Override
		public void pause(long id) throws TException {
			WorkerContainers.get(id).ifPresent(c -> c.pause());
		}

		@Override
		public void kill(long id) throws TException {
			WorkerContainers.get(id).ifPresent(c -> c.kill());
		}
	}
}