package com.redhat.demo.saga.connect;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.header.Headers;
import org.apache.kafka.connect.transforms.Transformation;

import java.util.Map;


public class TicketEventRouter<R extends ConnectRecord<R>> implements Transformation<R> {

    private static final String TOPIC = "tickets";

    public TicketEventRouter() { }

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

            //order id
            String key = after.getString("correlationid");

            String itemeventtype = after.getString("itemeventtype");
            Long ticketid = after.getInt64("ticketid");
            String accountid = after.getString("accountid");
            Long createdon = after.getInt64("createdon");

            //Optional fields
            Double totalcost = 0.0;
            try {
                totalcost = after.getFloat64("totalcost");
            } catch (Exception ex) {}

            Schema valueSchema = SchemaBuilder.struct()
                .field("itemeventtype", after.schema().field("itemeventtype").schema())
                .field("createdon", after.schema().field("createdon").schema())
                .field("itemid", after.schema().field("itemid").schema())
                .field("accountid", after.schema().field("accountid").schema())
                .field("totalcost", after.schema().field("totalcost").schema())
                .build();

            Struct value = new Struct(valueSchema)
                .put("itemeventtype", itemeventtype)
                .put("createdon", createdon)
                .put("itemid", ticketid)
                .put("accountid", accountid)
                .put("totalcost", totalcost);

            Headers headers = record.headers();
            headers.addString("correlationid", key);

            return record.newRecord(TOPIC, null, Schema.STRING_SCHEMA, key, valueSchema, value,
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
