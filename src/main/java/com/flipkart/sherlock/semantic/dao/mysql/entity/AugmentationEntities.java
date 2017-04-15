package com.flipkart.sherlock.semantic.dao.mysql.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by anurag.laddha on 11/04/17.
 */
public class AugmentationEntities {

    @Getter
    @AllArgsConstructor
    @ToString
    public static class Synonym {

        public static enum Type {
            replace, query, term
        }

        private String term;
        /**
         * Comma separated synonym for term
         */
        private String synonyms;
        /**
         * type of synonym
         */
        private Type synType;
    }

    public static class SynonymMapper implements ResultSetMapper<Synonym> {
        @Override
        public Synonym map(int index, ResultSet r, StatementContext ctx) throws SQLException {
            return new Synonym(r.getString("query"), r.getString("synonyms"), Synonym.Type.valueOf(r.getString("stype").toLowerCase()));
        }
    }


    @Getter
    @AllArgsConstructor
    @ToString
    public static class AugmentationExperiment {

        //todo there is '' as valid value for type in db.
        public static enum Type {
            replace, replaceNoShow, term, query
        }

        private String incorrectQuery;
        private String correctQuery;
        private String context; //This carries confidence score
        private String type;
        private String source; //Source is getting used as context
    }

    public static class AugmentationExperimentMapper implements ResultSetMapper<AugmentationExperiment> {
        @Override
        public AugmentationExperiment map(int index, ResultSet r, StatementContext ctx) throws SQLException {
            return new AugmentationExperiment(r.getString("incorrectQuery"), r.getString("correctQuery"), r.getString("context"),
                r.getString("stype"), r.getString("source"));
        }
    }


    /**
     * Pojo representing rows from EntityMetaTable
     */
    @Getter
    @AllArgsConstructor
    @ToString
    public static class EntityMeta{
        /**
         * Table name without date
         */
        private String baseTable;
        /**
         * Table name with date when it was last updated
         */
        private String latestEntityTable;
    }

    public static class EntityMetaMapper implements ResultSetMapper<EntityMeta> {
        @Override
        public EntityMeta map(int index, ResultSet r, StatementContext ctx) throws SQLException {
            return new EntityMeta(r.getString("baseTable"), r.getString("latestEntityTable"));
        }
    }


    /**
     * Pojo representing entries from spelling correction tables (spellcheck_*)
     */
    @Getter
    @AllArgsConstructor
    @ToString
    public static class SpellCorrection{
        private String incorrectSpelling;
        /**
         * Json serialised set of corrections
         */
        private String correctSpelling;
    }

    public static class SpellCorrectionMapper implements ResultSetMapper<SpellCorrection> {
        @Override
        public SpellCorrection map(int index, ResultSet r, StatementContext ctx) throws SQLException {
            return new SpellCorrection(r.getString("incorrect"), r.getString("correct"));
        }
    }


    /**
     * Pojo representing bi_compound table rows
     */
    @Getter
    @AllArgsConstructor
    @ToString
    public static class BiCompound{
        /**
         * merged/joined term
         */
        private String unigram;
        /**
         * Split of joined term
         */
        private String bigram;
        /**
         * The one that is correct: unigram, bigram, both or none
         */
        private String correct;

        //TODO int is fine? is phit same as pclick? what is sHit?
        private float unigramPHits;
        private float unigramSHits;
        private float bigramPHits;
        private float bigramSHits;
    }

    /**
     * Mapper to create Pojo for old bi compound table rows
     */
    public static class OldBiCompoundMapper implements ResultSetMapper<BiCompound> {
        @Override
        public BiCompound map(int index, ResultSet r, StatementContext ctx) throws SQLException {
            return new BiCompound(r.getString("unigram"), r.getString("bigram"), r.getString("correct"),
                r.getFloat("uni_p_clicks"), r.getFloat("uni_s_hits"), r.getFloat("bi_p_clicks"), r.getFloat("bi_s_hits"));
        }
    }

    /**
     * Mapper to create Pojo for bi compound table rows
     */
    public static class BiCompoundMapper implements ResultSetMapper<BiCompound> {
        @Override
        public BiCompound map(int index, ResultSet r, StatementContext ctx) throws SQLException {
            return new BiCompound(r.getString("unigram"), r.getString("bigram"), r.getString("correct"),
                r.getFloat("uni_p_hits"), r.getFloat("uni_s_hits"), r.getFloat("bi_p_hits"), r.getFloat("bi_s_hits"));
        }
    }
}
