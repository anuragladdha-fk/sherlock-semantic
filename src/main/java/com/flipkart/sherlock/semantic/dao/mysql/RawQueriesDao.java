package com.flipkart.sherlock.semantic.dao.mysql;

import com.flipkart.sherlock.semantic.dao.mysql.entity.AugmentationEntities.*;
import com.google.inject.Inject;
import org.skife.jdbi.v2.DBI;

import java.util.List;

/**
 * Created by anurag.laddha on 11/04/17.
 */

/**
 * Separate DAO for these queries since the table name is dynamic.
 */
public class RawQueriesDao {

    private DBI dbi;

    @Inject
    public RawQueriesDao(DBI dbi) {
        this.dbi = dbi;
    }

    public List<SpellCorrection> getAugmentationSpellCorrections(String spellCorrectionTableName){
        //Base table name: spellcheck_new
        String query = String.format(" SELECT incorrect, correct FROM  %s " +
            " WHERE incorrect IS NOT NULL AND correct IS NOT NULL AND row_status = 'high_confidence' ", spellCorrectionTableName);

        return this.dbi.withHandle(handle -> {
            handle.registerMapper(new SpellCorrectionMapper());
            return handle.createQuery(query)
                .mapTo(SpellCorrection.class)
                .list();
        });
    }

    public List<BiCompound> getAugmentationCompounds(String tableName){
        //Base table name: bi_compound
        String query = String.format(" SELECT unigram, bigram, correct, uni_p_hits, uni_s_hits, bi_p_hits, bi_s_hits " +
            " FROM %s WHERE correct !='none' ", tableName);

        return this.dbi.withHandle(handle -> {
            handle.registerMapper(new BiCompoundMapper());
            return handle.createQuery(query)
                .mapTo(BiCompound.class)
                .list();
        });
    }

}
