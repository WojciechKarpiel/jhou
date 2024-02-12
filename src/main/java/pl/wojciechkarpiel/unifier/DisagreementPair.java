package pl.wojciechkarpiel.unifier;

import pl.wojciechkarpiel.ast.Term;

public class DisagreementPair {
    private final Term first;
    private final Term second;

    public DisagreementPair(Term first, Term second) {
        this.first = first;
        this.second = second;
    }

    public Term getFirst() {
        return first;
    }

    public Term getSecond() {
        return second;
    }

    @Override
    public String toString() {
        return "DisagreementPair{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }
}
