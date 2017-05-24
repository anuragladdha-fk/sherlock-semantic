package com.flipkart.sherlock.semantic.common.dao.mysql.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by anurag.laddha on 03/04/17.
 */

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MysqlConnectionPoolConfig {
    /**
     * Minimum number of Connections a pool will maintain at any given time
     */
    private int minPoolSize;

    /**
     * Maximum number of Connections a pool will maintain at any given time
     */
    private int maxPoolSize;

    /**
     * Number of Connections a pool will try to acquire upon startup.
     * Should be between minPoolSize and maxPoolSize.
     */
    private int initialPoolSize;

    /**
     * Determines how many connections at a time will be acquired when the pool is exhausted
     */
    private int acquireIncrement;

    /**
     * Seconds a Connection can remain pooled but unused before being discarded.
     */
    private int maxIdleTimeSec;


    /**
     * Config builder
     */
    public static class MysqlConnectionPoolConfigBuilder {
        private int minPoolSize;
        private int maxPoolSize;
        private int initialPoolSize = 2;
        private int acquireIncrement = 2;
        private int maxIdleTimeSec = 0; //Zero means idle connections never expire

        public  MysqlConnectionPoolConfigBuilder(int minPoolSize, int maxPoolSize){
            this.minPoolSize = minPoolSize;
            this.maxPoolSize = maxPoolSize;
        }

        public MysqlConnectionPoolConfigBuilder setInitialPoolSize(int initialPoolSize) {
            this.initialPoolSize = initialPoolSize;
            return this;
        }

        public MysqlConnectionPoolConfigBuilder setAcquireIncrement(int acquireIncrement) {
            this.acquireIncrement = acquireIncrement;
            return this;
        }

        public MysqlConnectionPoolConfigBuilder setMaxIdleTimeSec(int maxIdleTimeSec) {
            this.maxIdleTimeSec = maxIdleTimeSec;
            return this;
        }

        public MysqlConnectionPoolConfig build() {
            return new MysqlConnectionPoolConfig(minPoolSize, maxPoolSize, initialPoolSize, acquireIncrement, maxIdleTimeSec);
        }
    }
}


