package net.fishtron.eva.multi.operators;

import net.fishtron.eva.IndivGenerator;
import net.fishtron.types.Type;
import net.fishtron.trees.AppTree;
import net.fishtron.gen.Gen;
import net.fishtron.eva.simple.AppTreeIndivGenerator;
import net.fishtron.eva.multi.AppTreeMI;
import net.fishtron.utils.F;
import org.json.JSONObject;

import java.util.List;
import java.util.Random;
import java.util.Set;


/**Created by tom on 09.03.2017.*/

public class AppTreeMIGenerator implements IndivGenerator<AppTreeMI> {

    private final Type goalType;
    private final int maxTreeSize;
    private final Gen gen;
    private final JSONObject allParamsInfo;
    private final Random rand;

    public AppTreeMIGenerator(Type goalType, int maxTreeSize, Gen gen, JSONObject allParamsInfo) {
        this.goalType = goalType;
        this.maxTreeSize = maxTreeSize;
        this.gen = gen;
        this.allParamsInfo = allParamsInfo;
        this.rand = gen.getRand();
    }

    @Override
    public List<AppTreeMI> generate(int numToGenerate) {

        // TODO řádně parametrizovat gen metodu !!!!!!!!!!!!!!!!!!!!!!!!!!!

        //Set<AppTree> treeSet = AppTreeIndivGenerator.generate_cleverSizes(numToGenerate, goalType, maxTreeSize,gen, allParamsInfo, rand, gen.getChecker());
        Set<AppTree> treeSet = AppTreeIndivGenerator.generate_randomSize(numToGenerate, goalType, maxTreeSize, gen, allParamsInfo, rand, /*silent*/ false);


        return F.map(treeSet, AppTreeMI::new);
    }

}
