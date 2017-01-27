package com.godaddy.logging.models;

import com.godaddy.logging.LoggingScope;
import com.godaddy.logging.Scope;
import lombok.Data;

@Data
public class AnnotatedLombokObject {

    private String noAnnotationProperty = "NoAnnotationProperty";

    @LoggingScope(scope = Scope.HASH)
    private String hashAnnotationProperty = "HashAnnotationProperty";

    @LoggingScope(scope = Scope.SKIP)
    private String skipAnnotationProperty = "SkipAnnotationProperty";
}
