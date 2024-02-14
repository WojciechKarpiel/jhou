package pl.wojciechkarpiel.jhou.termHead;

import pl.wojciechkarpiel.jhou.ast.Constant;
import pl.wojciechkarpiel.jhou.ast.Term;
import pl.wojciechkarpiel.jhou.ast.Variable;

import java.util.Objects;

public interface Head {


    <T> T visit(HeadVisitor<T> visitor);

    Term getTerm();


    class HeadVariable implements Head {
        private final Variable v;

        public HeadVariable(Variable v) {
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
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            HeadVariable that = (HeadVariable) o;
            return Objects.equals(v, that.v);
        }

        @Override
        public int hashCode() {
            return Objects.hash(v);
        }

        @Override
        public String toString() {
            return v.toString();
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
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            HeadConstant that = (HeadConstant) o;
            return Objects.equals(c, that.c);
        }

        @Override
        public int hashCode() {
            return Objects.hash(c);
        }

        @Override
        public String toString() {
            return c.toString();
        }
    }


    interface HeadVisitor<T> {
        T visitConstant(Constant constant);

        T visitVariable(Variable variable);

    }
}