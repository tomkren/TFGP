package cz.tomkren.fishtron.ugen.apps.gpml;

import com.google.common.collect.Sets;
import cz.tomkren.fishtron.ugen.trees.AppTree;
import cz.tomkren.fishtron.ugen.trees.Sexpr;
import cz.tomkren.utils.F;
import cz.tomkren.utils.TODO;

import java.util.List;
import java.util.Set;

/**
 * Created by tom on 27.07.2017.
 */
public class SizeUtils {

    public static int workflowSize(AppTree appTree) {
        return workflowSize(appTree.toSexpr());
    }

    private static final int foldFactor = 5;

    private static final String stackingName = "stacking";

    private static final Set<String> methodNames = Sets.newHashSet("SVC" ,"logR" ,"gaussianNB" ,"DT" ,"Perceptron" ,"SGD" ,"PAC" ,"LDA" ,"QDA" ,"MLP" ,"PCA" ,"kBest" ,"kMeans");
    private static final Set<String> zeroLeafNames = Sets.newHashSet("nil", "copy" ,"vote", "booBegin", "booEnd", "stacker");

    private static final Set<String> opNames = Sets.newHashSet( "cons", "booster", "split", stackingName, "boosting");
    private static final Set<String> diaOpNames = Sets.newHashSet("dia" ,"dia0");


    private static int workflowSize(Sexpr tree) {
        String sym = tree.getSym();
        if (tree.isLeaf()) {
            if (methodNames.contains(sym))   {return 1;}
            if (zeroLeafNames.contains(sym)) {return 0;}
            throw new Error("Unsupported leaf symbol: "+sym);
        } else {
            List<Sexpr> args = tree.getArgs();
            if (opNames.contains(sym)) {
                return F.sumInt(F.map(args, SizeUtils::workflowSize));
            }
            if (diaOpNames.contains(sym)) {

                int numArgs = args.size();

                if (!(numArgs == 2 || numArgs == 3)) {
                    throw new Error("Unsupported number of args in sym "+sym+": "+numArgs);
                }

                Sexpr merger = args.get(numArgs-1);

                if (merger.getSym().equals(stackingName)) {

                    Sexpr splitPart = args.get(numArgs-2);

                    int sum = numArgs == 2 ? 0 : workflowSize(args.get(0));
                    sum += foldFactor * workflowSize(splitPart);
                    sum += workflowSize(merger);
                    return sum;

                } else {
                    return F.sumInt(F.map(args, SizeUtils::workflowSize));
                }

            }
            throw new Error("Unsupported operator symbol: "+sym);
        }
    }



}
