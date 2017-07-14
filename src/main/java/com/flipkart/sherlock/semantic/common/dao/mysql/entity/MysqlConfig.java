package com.flipkart.sherlock.semantic.common.dao.mysql.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Created by anurag.laddha on 03/04/17.
 */

@AllArgsConstructor
@Getter
@ToString
public class MysqlConfig {
    private String dbHost;
    private int dbPort;
    private String userName;
    private String password;
    private String dbName;
}
