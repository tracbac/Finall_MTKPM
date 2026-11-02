package com.library.digital_library.repository.repository2;

import com.library.digital_library.model.model2.CatalogBookMetadata2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository thao tác với bảng catalog_book_metadata_2 (Thành viên 2).
 */
@Repository
public interface CatalogBookMetadata2Repository extends JpaRepository<CatalogBookMetadata2, Long> {

    Optional<CatalogBookMetadata2> findByBook_Id(Long bookId);

    Optional<CatalogBookMetadata2> findByIsbn(String isbn);

    Optional<CatalogBookMetadata2> findByGoogleVolumeId(String googleVolumeId);

    List<CatalogBookMetadata2> findAllByItemType(String itemType);

    List<CatalogBookMetadata2> findAllByBook_IdIn(Collection<Long> bookIds);

    void deleteByBook_Id(Long bookId);
}

