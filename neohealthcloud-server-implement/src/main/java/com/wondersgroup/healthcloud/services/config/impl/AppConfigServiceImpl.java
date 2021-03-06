package com.wondersgroup.healthcloud.services.config.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.*;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.wondersgroup.healthcloud.common.utils.IdGen;
import com.wondersgroup.healthcloud.jpa.entity.config.AppConfig;
import com.wondersgroup.healthcloud.jpa.repository.config.AppConfigRepository;
import com.wondersgroup.healthcloud.services.config.AppConfigService;

/**
 * Created by zhaozhenxing on 2016/8/16.
 */
@Service("appConfigService")
public class AppConfigServiceImpl implements AppConfigService {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AppConfigServiceImpl.class);

	@Autowired
	private AppConfigRepository appConfigRepository;

	@Override
	public Map<String, String> findAppConfigByKeyWords(String mainArea, String specArea, List<String> keyWords,
			String source) {
		Map<String, String> cfgMap = null;
		try {
			AppConfig appConfig = new AppConfig();
			appConfig.setDelFlag("0");
			appConfig.setMainArea(mainArea);
			appConfig.setSpecArea(specArea);
			appConfig.setSource(source);
			List<AppConfig> appConfigList = findAll(appConfig, keyWords);

			if (appConfigList != null && appConfigList.size() > 0) {
				cfgMap = new HashMap<>();
				for (AppConfig ac : appConfigList) {
					cfgMap.put(ac.getKeyWord(), ac.getData());
				}
			}
		} catch (Exception ex) {
			logger.error("AppConfigServiceImpl.findAppConfigByKeyWord\t-->\t" + ex.getLocalizedMessage());
		}
		return cfgMap;
	}

	@Override
	public Map<String, String> findAppConfigByKeyWords(String mainArea, String specArea, List<String> keyWords) {
		return findAppConfigByKeyWords(mainArea, specArea, keyWords, "1");
	}

	@Override
	public List<AppConfig> findAllDiscreteAppConfig(String mainArea, String specArea, String source) {
		try {
			AppConfig appConfig = new AppConfig();
			appConfig.setDelFlag("0");
			appConfig.setDiscrete(1);
			appConfig.setMainArea(mainArea);
			appConfig.setSpecArea(specArea);
			appConfig.setSource(source);
			List<AppConfig> appConfigList = findAll(appConfig, null);

			if (appConfigList != null && appConfigList.size() > 0) {
				return appConfigList;
			}
		} catch (Exception ex) {
			logger.error("AppConfigServiceImpl.findAllDiscreteAppConfig\t-->\t" + ex.getLocalizedMessage());
		}
		return null;
	}

	@Override
	public List<AppConfig> findAllDiscreteAppConfig(String mainArea, String specArea) {
		return findAllDiscreteAppConfig(mainArea, specArea, "1");

	}

	@Override
	public AppConfig findSingleAppConfigByKeyWord(String mainArea, String specArea, String keyWord, String source) {
		try {
			AppConfig appConfig = new AppConfig();
			appConfig.setDelFlag("0");
			appConfig.setMainArea(mainArea);
			appConfig.setSpecArea(specArea);
			appConfig.setKeyWord(keyWord);
			appConfig.setSource(source);
			List<AppConfig> appConfigs = findAll(appConfig, null);

			if (appConfigs != null && appConfigs.size() > 0) {
				return appConfigs.get(0);
			}
		} catch (Exception ex) {
			logger.error("AppConfigServiceImpl.findSingleAppConfigByKeyWord\t-->\t" + ex.getLocalizedMessage());
		}
		return null;
	}

	@Override
	public AppConfig findSingleAppConfigByKeyWord(String mainArea, String specArea, String keyWord) {
		return findSingleAppConfigByKeyWord(mainArea, specArea, keyWord, "1");
	}

	@Override
	public AppConfig saveAndUpdateAppConfig(AppConfig appConfig) {
		AppConfig rtnAppConfig = null;
		if (appConfig.getId() == null) {
			appConfig.setId(IdGen.uuid());
			appConfig.setCreateTime(new Date());
			appConfig.setDelFlag("0");
		}
		appConfig.setUpdateTime(new Date());
		try {
			rtnAppConfig = appConfigRepository.save(appConfig);
		} catch (Exception ex) {
			logger.error("AppConfigServiceImpl.saveAndUpdateAppConfig\t-->\t" + ex.getLocalizedMessage());
		}
		return rtnAppConfig;
	}

	List<AppConfig> findAll(final AppConfig appConfig, final List<String> keyWords) {
		return appConfigRepository.findAll(new Specification<AppConfig>() {
			@Override
			public Predicate toPredicate(Root<AppConfig> rt, CriteriaQuery<?> cq, CriteriaBuilder cb) {
				List<Predicate> predicateList = new ArrayList<Predicate>();
				if (StringUtils.isNotEmpty(appConfig.getMainArea())) {
					predicateList.add(cb.equal(rt.<String> get("mainArea"), appConfig.getMainArea()));
				}
				if (StringUtils.isNotEmpty(appConfig.getSpecArea())) {
					predicateList.add(cb.equal(rt.<String> get("specArea"), appConfig.getSpecArea()));
				}
				// 单关键字查询与多关键字查询互斥
				if (StringUtils.isNotEmpty(appConfig.getKeyWord())) {
					predicateList.add(cb.equal(rt.<String> get("keyWord"), appConfig.getKeyWord()));
				} else if (keyWords != null && keyWords.size() > 0) {
					Expression<String> expression = rt.<String> get("keyWord");
					predicateList.add(expression.in(keyWords));
				}
				if (appConfig.getDiscrete() != null) {
					predicateList.add(cb.equal(rt.<String> get("discrete"), appConfig.getDiscrete()));
				}

				if (appConfig.getSource() == null) {
					predicateList.add(cb.equal(rt.<String> get("source"), "1"));
				} else {
					predicateList.add(cb.equal(rt.<String> get("source"), appConfig.getSource()));
				}
				if (predicateList.size() > 0) {
					Predicate[] predicates = new Predicate[predicateList.size()];
					cq.where(predicateList.toArray(predicates));
				}
				Order order = cb.desc(rt.<String>get("updateTime"));
				cq.orderBy(order);

				return null;
			}
		});
	}

}
