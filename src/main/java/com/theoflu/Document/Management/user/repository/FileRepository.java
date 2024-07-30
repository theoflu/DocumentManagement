package com.theoflu.Document.Management.user.repository;

import com.theoflu.Document.Management.user.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<FileEntity,Long> {

    FileEntity findFileEntityByFile (String filename);
}
