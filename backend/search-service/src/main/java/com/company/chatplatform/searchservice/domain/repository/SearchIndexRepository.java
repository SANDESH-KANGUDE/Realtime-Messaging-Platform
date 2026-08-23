package com.company.chatplatform.searchservice.domain.repository;

import com.company.chatplatform.searchservice.domain.document.SearchIndexDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchIndexRepository extends MongoRepository<SearchIndexDocument, String> {

    @Query("{ '$or': [ { 'title': { '$regex': ?0, '$options': 'i' } }, { 'content': { '$regex': ?0, '$options': 'i' } } ] }")
    List<SearchIndexDocument> searchByText(String text);

    @Query("{ 'entityType': ?1, '$or': [ { 'title': { '$regex': ?0, '$options': 'i' } }, { 'content': { '$regex': ?0, '$options': 'i' } } ] }")
    List<SearchIndexDocument> searchByTextAndType(String text, String entityType);

    void deleteByEntityIdAndEntityType(String entityId, String entityType);
}
