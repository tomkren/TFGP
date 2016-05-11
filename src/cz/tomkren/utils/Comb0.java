package cz.tomkren.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface Comb0 {

    Object compute(List<Object> inputs);


    default Object compute0() {
        return compute( null );
    }

    default Object compute1(Object x){
        return compute(Collections.singletonList(x));
    }

    default Object compute2(Object x, Object y){
        return compute(Arrays.asList(x,y));
    }

    static Comb0 mkFun(Function<Object, Object> f) {
        return inputs -> f.apply(inputs.get(0));
    }

    static Comb0 mkFun2(BiFunction<Object, Object, Object> f) {
        return inputs -> f.apply(inputs.get(0),inputs.get(1));
    }


    static Comb0 mkVec(Vec xs) {return fakeInputs -> xs;}

    static Comb0 mkVecFun(Function<Vec, Vec> f) {
        return inputs -> f.apply((Vec)inputs.get(0));
    }

    static Comb0 mkVecFun2(BiFunction<Vec, Vec, Vec> f) {
        return inputs -> f.apply((Vec)inputs.get(0),(Vec)inputs.get(1));
    }


    static Comb0 fromStatic(String name, Class<?> homeClass, Class<?>... parameterTypes) {
        try {

            Method method = homeClass.getMethod(name, parameterTypes);
            return argsList -> {
                try {
                    if (argsList != null && parameterTypes.length > 0) {
                        //Log.it("parameterTypes.length: "+parameterTypes.length);
                        return method.invoke(null, argsList.toArray());
                    } else {
                        return method.invoke(null);
                    }
                } catch (IllegalArgumentException e)  {throw new Error("IllegalArgumentException !"+e.getCause().getMessage());}
                catch (IllegalAccessException e)    {throw new Error("IllegalAccessException !");}
                catch (InvocationTargetException e) {throw new Error("[ITE] "+e.getCause().getMessage());}
            };
        }
        catch (SecurityException e)     {throw new Error("SecurityException !");}
        catch (NoSuchMethodException e) {throw new Error("NoSuchMethodException !");}
    }



    static int plus(int x,int y) {return x + y;}
    static int zero() {return 0;}

    static void main(String[] args) {
        Checker ch = new Checker();

        Comb0 plus = Comb0.fromStatic("plus", Comb0.class, int.class, int.class);
        ch.it(plus.compute2(2,2), "4");

        Comb0 zero = Comb0.fromStatic("zero", Comb0.class);
        ch.it(zero.compute0(), "0");
        ch.it(plus.compute2(1,zero.compute0()), "1");

        ch.results();
    }

}
