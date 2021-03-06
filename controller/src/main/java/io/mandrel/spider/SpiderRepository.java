/*
 * Licensed to Mandrel under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Mandrel licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.mandrel.spider;

import io.mandrel.common.data.Spider;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SpiderRepository {

	Spider add(Spider spider);

	Spider update(Spider spider);

	void updateStatus(long spiderId, String status);

	void delete(long id);

	Optional<Spider> get(long id);

	List<Spider> listActive();

	List<Spider> listLastActive(int limit);

	Page<Spider> page(Pageable pageable);

	Page<Spider> pageForActive(Pageable pageable);

}
