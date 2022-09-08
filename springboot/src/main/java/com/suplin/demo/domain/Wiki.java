package com.suplin.demo.domain;

public class Wiki {

    private long table_id;
    private String table_name;

    public long getTable_id() {
        return table_id;
    }

    public void setTable_id(long table_id) {
        this.table_id = table_id;
    }

    public String getTable_name() {
        return table_name;
    }

    public void setTable_name(String table_name) {
        this.table_name = table_name;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Wiki{");
        sb.append("table_id=").append(table_id);
        sb.append(", table_name='").append(table_name).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
