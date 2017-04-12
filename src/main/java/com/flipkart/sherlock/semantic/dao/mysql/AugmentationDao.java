package com.flipkart.sherlock.semantic.dao.mysql;

import com.flipkart.sherlock.semantic.dao.mysql.entity.AugmentationEntities.*;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;
import java.util.Set;

/**
 * Created by anurag.laddha on 11/04/17.
 */
public interface AugmentationDao {

    @SqlQuery("SELECT query FROM augment_negativeList")
    Set<String> getNegatives();


    @RegisterMapper(SynonymMapper.class)
    @SqlQuery("SELECT query, synonyms, stype FROM augment_synonyms")
    List<Synonym> getSynonyms();


    @RegisterMapper(AugmentationExperimentMapper.class)
    @SqlQuery("SELECT incorrectQuery, correctQuery, stype, source, context FROM augment_experiments_all")
    List<AugmentationExperiment> getAugmentationExperiements();


    @RegisterMapper(EntityMetaMapper.class)
    @SqlQuery("SELECT * FROM EntityMetaTable " +
            " WHERE baseTable = :tableName " +
            " ORDER BY create_date DESC LIMIT 1")
    EntityMeta getEntityMeta(@Bind("tableName") String tableName);


    @RegisterMapper(SpellCorrectionMapper.class)
    @SqlQuery("SELECT incorrect, correct FROM spellcheck_low_conf " +
            " WHERE incorrect IS NOT NULL AND correct IS NOT NULL AND status = 'approved' ")
    List<SpellCorrection> getSpellCorrectionsLowConf();


    @RegisterMapper(OldBiCompoundMapper.class)
    @SqlQuery("SELECT unigram, bigram, correct, uni_p_clicks, uni_s_hits, bi_p_clicks, bi_s_hits " +
        " FROM bi_compound_analysis_feb_aug16_ordered WHERE correct !='none' ")
    List<BiCompound> getOldCompounds();
}
