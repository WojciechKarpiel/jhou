package pl.wojciechkarpiel.jhou.types.inference;

import pl.wojciechkarpiel.jhou.ast.Term;

import java.util.ArrayList;
import java.util.List;

class AnnotatedTerm {

    final Term originalTerm;
    final Term annotation;

    AnnotatedTerm(Term originalTerm, Term annotation) {
        this.originalTerm = originalTerm;
        this.annotation = annotation;
    }
}

class Annotations {

    private final List<AnnotatedTerm> annotations;

    Annotations(){
        this(new ArrayList<>());
    }
    Annotations(List<AnnotatedTerm> annotations){
        this.annotations = annotations;
    }

    List<AnnotatedTerm> getAnnotations() {
        return annotations;
    }

     void addAnnotation(Term originalTerm, Term annotation){
        getAnnotations().add(new AnnotatedTerm(originalTerm, annotation));
    }

     Term getAnnotation(Term originalTerm) {
        for (AnnotatedTerm annotation : annotations) {
            if (annotation.originalTerm == originalTerm) { // deliberate reference comparison
                return annotation.annotation;
            }
        }
        throw new RuntimeException();
    }
}
