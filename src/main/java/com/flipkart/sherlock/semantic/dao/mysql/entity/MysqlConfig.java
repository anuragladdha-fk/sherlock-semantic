package com.flipkart.sherlock.semantic.dao.mysql.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by anurag.laddha on 03/04/17.
 */

@AllArgsConstructor
@Getter
public class MysqlConfig {
    private String dbHost;
    private int dbPort;
    private String userName;
    private String password;
    private String dbName;
}
