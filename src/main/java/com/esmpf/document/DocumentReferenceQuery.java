package com.esmpf.document;

import com.esmpf.document.DocumentDtos.DocumentReference;
import java.util.UUID;

public interface DocumentReferenceQuery {
    DocumentReference requireDocument(UUID documentId);
    void requireAttachment(UUID attachmentId);
}