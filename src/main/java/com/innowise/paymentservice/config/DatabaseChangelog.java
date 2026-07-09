package com.innowise.paymentservice.config;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.mongodb.client.MongoDatabase;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.domain.Sort;

@ChangeLog(order = "001")
public class DatabaseChangelog {

    @ChangeSet(order = "001", id = "create-payments-collection", author = "karalina")
    public void createPaymentsCollection(MongoDatabase db) {
        db.createCollection("payments");
    }

    @ChangeSet(order = "002", id = "create-indexes", author = "karalina")
    public void createIndexes(MongoTemplate template) {
        template.indexOps("payments").ensureIndex(new Index().on("user_id", Sort.Direction.ASC));
        template.indexOps("payments").ensureIndex(new Index().on("order_id", Sort.Direction.ASC));
        template.indexOps("payments").ensureIndex(new Index().on("status", Sort.Direction.ASC));
    }
}