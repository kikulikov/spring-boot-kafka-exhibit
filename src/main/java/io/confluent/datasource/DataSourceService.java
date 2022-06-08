package io.confluent.datasource;

import io.confluent.model.avro.OnlineOrder;
import org.springframework.stereotype.Service;

@Service
public interface DataSourceService {

    OnlineOrder retrieveEvent();
}
