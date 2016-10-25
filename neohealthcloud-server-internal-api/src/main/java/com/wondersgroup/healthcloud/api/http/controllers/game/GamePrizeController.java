package com.wondersgroup.healthcloud.api.http.controllers.game;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.wondersgroup.healthcloud.common.http.dto.JsonResponseEntity;
import com.wondersgroup.healthcloud.jpa.entity.activity.HealthActivityInfo;
import com.wondersgroup.healthcloud.jpa.entity.game.Game;
import com.wondersgroup.healthcloud.jpa.entity.game.GamePrize;
import com.wondersgroup.healthcloud.jpa.entity.game.PrizeWin;
import com.wondersgroup.healthcloud.jpa.enums.GameType;
import com.wondersgroup.healthcloud.jpa.repository.activity.HealthActivityInfoRepository;
import com.wondersgroup.healthcloud.jpa.repository.game.GamePrizeRepository;
import com.wondersgroup.healthcloud.jpa.repository.game.GameRepository;
import com.wondersgroup.healthcloud.jpa.repository.game.PrizeWinReporistory;
import com.wondersgroup.healthcloud.services.game.GameService;
import com.wondersgroup.healthcloud.services.user.SessionUtil;
import com.wondersgroup.healthcloud.services.user.dto.Session;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by zhuchunliu on 2016/10/24.
 */
@RestController
@RequestMapping("/game/prize")
public class GamePrizeController {

    @Autowired
    private PrizeWinReporistory prizeWinRepo;
    @Autowired
    private GamePrizeRepository gamePrizeRepo;
    @Autowired
    private HealthActivityInfoRepository activityInfoRepo;
    @Autowired
    private GameRepository gameRepo;
    @Autowired
    private SessionUtil sessionUtil;
    /**
     * 抽奖
     * @return
     */
    @GetMapping(path = "/draw")
    public JsonResponseEntity draw(
            @RequestHeader(name="access-token",required = true) String token,
            @RequestParam(name = "activityid",required = true) String activityid){
        Session session = sessionUtil.get(token);
        if(null == session || false == session.getIsValid() || StringUtils.isEmpty(session.getUserId())){
            return new JsonResponseEntity(1001,"您已长时间未登录，请重新登录!");
        }
        String registerId = session.getUserId();
        PrizeWin prizeWin = prizeWinRepo.findByRegisterId(registerId,activityid);
        if(null != prizeWin){
            return new JsonResponseEntity(1002,"您已抽过奖，禁止重复参与抽奖!");
        }
        Game game = gameRepo.getTopGame(GameType.TURNTABLE.type);
        GamePrize gamePrize = this.drawPrize(game.getId());
        if(null == gamePrize){
            return new JsonResponseEntity(1003,"奖池奖品已经全部抽完，谢谢参与!");
        }
        PrizeWin win = new PrizeWin();
        win.setRegisterid(registerId);
        win.setActivityid(activityid);
        win.setPrizeid(gamePrize.getId());
        win.setCreateDate(new Date());
        win.setDelFlag("0");
        prizeWinRepo.save(win);

        int total = gamePrizeRepo.getTotalByGameId(game.getId());
        int rank = gamePrizeRepo.getLessThenLevelTotal(game.getId(),gamePrize.getLevel());

        return new JsonResponseEntity(0,null, ImmutableMap.of("total",total,"rank",rank));
    }

    /**
     * 中奖信息
     * @return
     */
    @GetMapping(path = "/info")
    public JsonResponseEntity info(
            @RequestHeader(name="access-token",required = true) String token,
            @RequestParam(name = "activityid",required = true) String activityid
            ){
        Session session = sessionUtil.get(token);
        if(null == session || false == session.getIsValid() || StringUtils.isEmpty(session.getUserId())){
            return new JsonResponseEntity(1001,"您已长时间未登录，请重新登录!");
        }
        String registerId = session.getUserId();
        PrizeWin prizeWin = prizeWinRepo.findByRegisterId(registerId,activityid);
        if(null != prizeWin){//中奖
            GamePrize gamePrize = gamePrizeRepo.findOne(prizeWin.getPrizeid());
            HealthActivityInfo activity = activityInfoRepo.findOne(prizeWin.getActivityid());
            Map map = Maps.newHashMap();
            if(null != gamePrize){
                map.put("prizeName",gamePrize.getName());
            }
            if(null != activity){
                map.put("location",activity.getLocate());
                map.put("startDate", new DateTime(activity.getOfflineEndTime()).getDayOfMonth());
                map.put("startTime", new DateTime(activity.getOfflineStartTime()).getHourOfDay());
                map.put("endTime", new DateTime(activity.getOfflineStartTime()).getHourOfDay());
            }
            return new JsonResponseEntity(0,null,map);
        }
        return new JsonResponseEntity(0,null,null);
    }

    /**
     * 随机抽奖
     * @return
     */
    private synchronized GamePrize drawPrize(Integer gameId){
        int amount = gamePrizeRepo.getAmoutByGameId(gameId);
        if(0 == amount){//奖池没有奖了，则返回null值
            return null;
        }
        int num = (int)(1+Math.random()*amount);
        int child = 0;
        List<GamePrize> list = gamePrizeRepo.findByGameId(gameId);
        for(GamePrize gamePrize : list){
            child += gamePrize.getAmount();
            if(child < num){
                continue;
            }
            gamePrize.setAmount(gamePrize.getAmount()-1);
            gamePrize.setUpdateDate(new Date());
            return gamePrize;
        }
        return null;
    }

}
