package com.company.chatplatform.searchservice.domain.repository;

import com.company.chatplatform.searchservice.domain.document.SearchIndexDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchIndexRepository extends MongoRepository<SearchIndexDocument, String> {

    @Query("{ $text: { $search: ?0 } }")
    List<SearchIndexDocument> searchByText(String text);

    @Query("{ $text: { $search: ?0 }, 'entityType': ?1 }")
    List<SearchIndexDocument> searchByTextAndType(String text, String entityType);
}
