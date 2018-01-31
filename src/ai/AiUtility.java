package ai;

import model.Resource;

/**
 * Created by thore on 22.01.2017.
 */
public class AiUtility {

    static public boolean canBuildStreet(Resource[] resources){
        if(resources[0].getValue() >= 1 && resources[1].getValue() >= 1){
            return true;
        }
        else return false;
    }

    static public boolean canBuildSettlement(Resource[] resources){
        if(resources[0].getValue() >= 1 && resources[1].getValue() >= 1 && resources[2].getValue() >= 1 && resources[3].getValue() >= 1){
            return true;
        }
        else return false;
    }

    static public boolean canBuildCity(Resource[] resources){
        if(resources[3].getValue() >= 2 && resources[4].getValue() >= 3){
            return true;
        }
        else return false;
    }

    static public boolean canBuyDevCard(Resource[] resources){
        if(resources[3].getValue() >= 1 && resources[4].getValue() >= 1 && resources[2].getValue() >= 1){
            return true;
        }
        else return false;
    }
}
