package com.redhat.demo.saga.connect;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.header.Headers;
import org.apache.kafka.connect.transforms.Transformation;

import java.util.Map;


public class EventRouter<R extends ConnectRecord<R>> implements Transformation<R> {

    public EventRouter() { }

    @Override
    public void configure(Map<String, ?> configs) { }

    @Override
    public R apply(R record) {
        if (record.value() == null)
            return record;

        Struct struct = (Struct) record.value();
        String op = struct.getString("op");

        if (op.equals("d")) {
            return null;
        }
        else if (op.equals("c")) {
            Struct after = struct.getStruct("after");

            String key = after.getString("correlationid");
            String ticketeventtype = after.getString("ticketeventtype");
            Long ticketid = after.getInt64("ticketid");
            String accountid = after.getString("accountid");
            Long createdon = after.getInt64("createdon");

            Schema valueSchema = SchemaBuilder.struct()
                .field("ticketeventtype", after.schema().field("ticketeventtype").schema())
                .field("createdon", after.schema().field("createdon").schema())
                .field("ticketid", after.schema().field("ticketid").schema())
                .field("accountid", after.schema().field("accountid").schema())
                .build();

            Struct value = new Struct(valueSchema)
                .put("ticketeventtype", ticketeventtype)
                .put("createdon", createdon)
                .put("ticketid", ticketid)
                .put("accountid", accountid);

            Headers headers = record.headers();
            headers.addString("correlationid", key);


            return record.newRecord("tickets", null, Schema.STRING_SCHEMA, key, valueSchema, value,
                    record.timestamp(), headers);
        }
        else {
            throw new IllegalArgumentException("Record of unexpected op type: " + record);
        }
    }

    @Override
    public ConfigDef config() {
        return new ConfigDef();
    }

    @Override
    public void close() { }
}
