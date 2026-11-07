package com.library.digital_library.repository.repository3;

import com.library.digital_library.model.model3.EBookFile3;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EBookFile3Repository extends JpaRepository<EBookFile3, Long> {

    Optional<EBookFile3> findByBookId(Long bookId);
}
