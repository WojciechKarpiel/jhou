package pl.wojciechkarpiel.termHead;

import pl.wojciechkarpiel.ast.Constant;
import pl.wojciechkarpiel.ast.Term;
import pl.wojciechkarpiel.ast.Variable;

public interface Head {


    <T> T visit(HeadVisitor<T> visitor);

    Term getTerm();


    class HeadVariable implements Head {
        private final Variable v;

        HeadVariable(Variable v, boolean rigid) {
            this.v = v;
        }

        @Override
        public <T> T visit(HeadVisitor<T> visitor) {
            return visitor.visitVariable(v);
        }

        @Override
        public Term getTerm() {
            return v;
        }

        public Variable getV() {
            return v;
        }

        @Override
        public boolean equals(Object obj) {
            throw new RuntimeException();
        }
    }

    class HeadConstant implements Head {
        private final Constant c;

        public HeadConstant(Constant c) {
            this.c = c;
        }

        public Constant getC() {
            return c;
        }

        @Override
        public <T> T visit(HeadVisitor<T> visitor) {
            return visitor.visitConstant(c);
        }

        @Override
        public Term getTerm() {
            return c;
        }

        @Override
        public boolean equals(Object obj) {
            throw new RuntimeException();
        }
    }


    interface HeadVisitor<T> {
        T visitConstant(Constant constant);

        T visitVariable(Variable variable);

    }
}
