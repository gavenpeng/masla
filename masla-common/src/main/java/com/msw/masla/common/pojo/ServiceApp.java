package com.msw.masla.common.pojo;

import com.alibaba.fastjson.annotation.JSONField;
import com.msw.masla.common.circuit.CircuitRuleDefine;
import com.msw.masla.common.monitor.metrics.AppRequestFailedCount;
import com.msw.masla.common.monitor.metrics.BandwidthCount;
import com.msw.masla.common.monitor.metrics.BodySectionCount;
import com.msw.masla.common.monitor.metrics.ResponseTimeCount;
import com.msw.masla.common.util.StringUtil;
import com.msw.masla.common.util.concurrent.LongAdder;
import com.msw.masla.common.constant.Constants;
import com.msw.masla.common.monitor.vo.ApiMetricMonitorVO;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import lombok.Data;
import org.HdrHistogram.ConcurrentHistogram;
import org.HdrHistogram.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Gavin.peng
 *
 */
@Data
public class ServiceApp {

	private static final Logger LOG = LoggerFactory.getLogger(ServiceApp.class);

	private static final int SERVICE_ID_LIMIT = 10000;

	private Long id;

	private String name;


	private String contextRoot;
	/**
	 * 关联流控规则ID
	 */
	private String flowRuleIds;

	/**
	 * 关联黑名单规则ID
	 */
	private String blackSelectorIds;


	private Integer status;


	private String addUser;

	/**
	 * 负载均衡器名称
	 */
	private String loadBalanceName;


	private Date gmtCreate;

	private Date gmtModify;

	private Integer checkParameters;

	private String securityKey;

	private String signatureParameterName;

	//app 绑定的LoopGroup 类型，默认为default,有0 default,1 fast,2 slow
	private int poolGroupType = 0;

	//获取app 熔断时的响应内容的地址，只需要servlet/controller
	private String discardPath;

	//熔断时的响应内容
	private String discardResponse;

	private String backupDiscardResponse;

	private String groupName;

	private Integer discard = -1;

	private Integer type = 1;//默认是web类型

	private Integer config;

	private boolean forbidMultiplex = true;

	private boolean autoDiscardOff;

	private boolean supportAutoCircuit = false;

	private boolean supportAutoFlowControl = false;

	private boolean supportFilterRequestHeader = false;

	private boolean supportSessionLimit = false;

	private boolean supportDomainRoute = false;

	/**用户行为采样*/
	private boolean supportBehaviorSample = false;

	private AtomicLong circuitTime = new AtomicLong(-1);

	public long preCircuitUpdateTime = -1;

	private boolean supportHealthcheck = true;

	private LongAdder queryCount = new LongAdder(0);

	private long qps = 0l;

	private int serviceIdCount = 0;

	private AtomicLong allCostTimeSum = new AtomicLong(0);

	private AtomicLong serverCostTimeSum = new AtomicLong(0);

	private AtomicLong acquireConnectCostTimeSum = new AtomicLong(0);

	//队列排队时间
	private AtomicLong queueWaitCostTimeSum = new AtomicLong(0);

	private AtomicLong outBandWidth = new AtomicLong(0);

	//service id qps map
	@JSONField(serialize = false)
	private ConcurrentHashMap<String,ConcurrentHashMap<String,ApiMetric>> appHostServiceIdMetricMap = new ConcurrentHashMap<String, ConcurrentHashMap<String,ApiMetric>>();

	@JSONField(serialize = false)
	private ConcurrentHashMap<String,LongAdder> serviceIdCircuitMetricMap = new ConcurrentHashMap<String, LongAdder>();

	@JSONField(serialize = false)
	private ConcurrentHashMap<String,LongAdder> serviceIdFlowControllerMetricMap = new ConcurrentHashMap<String, LongAdder>();

	@JSONField(serialize = false)
	private ConcurrentHashMap<String, AppRequestFailedCount> appRequestFailedMap = new ConcurrentHashMap<String, AppRequestFailedCount>();

	//service id tp90 response time
	@JSONField(serialize = false)
	private ConcurrentHashMap<String,ConcurrentHashMap<String,Histogram>> appHostServiceIdTP90Map = new ConcurrentHashMap<String, ConcurrentHashMap<String,Histogram>>();
	//private ConcurrentHashMap<String,List<Short>> serviceIdTP90Map = new ConcurrentHashMap<String, List<Short>>();

	private CircuitRuleDefine defaultCircuit;

	// 本次流控开始时间
	private Date flowControlStartTime;

	// 本次熔断开始时间
	private Date circuitStartTime;

	/**
	 * 出入站带宽统计
	 */
	@JSONField(serialize = false)
	private ConcurrentHashMap<String, BandwidthCount> appBandwidthMap = new ConcurrentHashMap<String, BandwidthCount>();


	/**
	 * 请求大小分布统计
	 */
	@JSONField(serialize = false)
	private volatile BodySectionCount requestBodyCount = new BodySectionCount();

	/**
	 * 响应大小分布统计
	 */
	@JSONField(serialize = false)
	private volatile BodySectionCount responseBodyCount = new BodySectionCount();

	/**
	 * 响应时间分布统计
	 */
	@JSONField(serialize = false)
	private volatile ResponseTimeCount responseTimeCount = new ResponseTimeCount();



	/**
	 * 客户端(nginx)连接关闭异常统计
	 */
	@JSONField(serialize = false)
	private  AtomicLong clientConnClosedCount = new AtomicLong(0);


	public void setAppBandwidthMap(
			ConcurrentHashMap<String, BandwidthCount> appBandwidthMap) {
		this.appBandwidthMap = appBandwidthMap;
	}


	public void setRequestBodyCount(
			BodySectionCount requestBodyCount) {
		this.requestBodyCount = requestBodyCount;
	}


	public void setResponseBodyCount(
			BodySectionCount responseBodyCount) {
		this.responseBodyCount = responseBodyCount;
	}

	public ResponseTimeCount getResponseTimeCount() {
		return responseTimeCount;
	}

	public void setResponseTimeCount(
			ResponseTimeCount responseTimeCount) {
		this.responseTimeCount = responseTimeCount;
	}


	public ServiceApp(){

	}


	//用于生成报警对象，alertEvent只需要appName
	public ServiceApp(String name) {
		this.name = name;
	}

	public void initDefaultCircuit(){
		if(this.defaultCircuit == null) {
			this.defaultCircuit = new CircuitRuleDefine(0.5f, this.name);
		}
	}

	public void setContextRoot(String contextRoot) {
		this.contextRoot = contextRoot;
	}

	public void setFlowRuleIds(String flowRuleIds) {
		this.flowRuleIds = flowRuleIds;
	}


	/**
	 * @param addUser
	 *            the addUser to set
	 */
	public void setAddUser(String addUser) {

		this.addUser = addUser;
	}


	public void setQps(long qps) {
		this.qps = qps;
	}


	/**
	 * @param gmtModify
	 *            the gmtModify to set
	 */
	public void setGmtModify(Date gmtModify) {

		this.gmtModify = gmtModify;
	}




	public void setAllCostTimeSum(AtomicLong allCostTimeSum) {
		this.allCostTimeSum = allCostTimeSum;
	}


	public void setServerCostTimeSum(AtomicLong serverCostTimeSum) {
		this.serverCostTimeSum = serverCostTimeSum;
	}


	public void setAcquireConnectCostTimeSum(AtomicLong acquireConnectCostTimeSum) {
		this.acquireConnectCostTimeSum = acquireConnectCostTimeSum;
	}


	public void setQueueWaitCostTimeSum(AtomicLong queueWaitCostTimeSum) {
		this.queueWaitCostTimeSum = queueWaitCostTimeSum;
	}

	public boolean isCircuit(){
		return !this.serviceIdCircuitMetricMap.isEmpty();
	}

	public ConcurrentHashMap<String, ConcurrentHashMap<String, Histogram>> getAppHostServiceIdTP90Map() {
		return appHostServiceIdTP90Map;
	}

	public void collectResponseTime(String host, String serviceId, long costTime){
		ConcurrentHashMap<String,Histogram> hostTp90MerticMap = this.appHostServiceIdTP90Map.get(host);
		if(hostTp90MerticMap == null){
			hostTp90MerticMap = new ConcurrentHashMap<String, Histogram>();
			ConcurrentHashMap<String,Histogram> preHostTP90MerticMap = this.appHostServiceIdTP90Map.putIfAbsent(host,hostTp90MerticMap);
			if(preHostTP90MerticMap != null){
				hostTp90MerticMap = preHostTP90MerticMap;
			}
		}

		Histogram histogram = hostTp90MerticMap.get(serviceId);
		if(histogram == null){
			histogram = new MaslaHistogram(Constants.MAX_HISTOGRAM,2);
			Histogram existHistogram = hostTp90MerticMap.putIfAbsent(serviceId,histogram);
			if(existHistogram != null){
				histogram = existHistogram;
			}
		}
		if(costTime > Constants.MAX_HISTOGRAM){
			costTime = Constants.MAX_HISTOGRAM;
		}
		histogram.recordValue(costTime);

	}

	public void addServiceCircuitCount(String serviceId){
		LongAdder longAdder = this.serviceIdCircuitMetricMap.get(serviceId);
		if(longAdder != null){
			longAdder.increment();
		}else{
			longAdder = new LongAdder(0);
			LongAdder existAdder = this.serviceIdCircuitMetricMap.putIfAbsent(serviceId,longAdder);
			if(existAdder != null){
				existAdder.increment();
			}else{
				longAdder.increment();
			}
		}
	}

	/**
	 * 被流控的流量统计
	 * @param serviceId
	 */
	public void addServiceFlowControllerCount(String serviceId){
		LongAdder longAdder = this.serviceIdFlowControllerMetricMap.get(serviceId);
		if(longAdder != null){
			longAdder.increment();
		}else{
			longAdder = new LongAdder(0);
			LongAdder existAdder = this.serviceIdFlowControllerMetricMap.putIfAbsent(serviceId,longAdder);
			if(existAdder != null){
				existAdder.increment();
			}else{
				longAdder.increment();
			}
		}
	}

	public long getAndResetServiceFlowControllerCount(String serviceId){
		LongAdder longAdder = this.serviceIdFlowControllerMetricMap.get(serviceId);
		if(longAdder != null){
			long circuitNums = longAdder.sumThenReset();
			return circuitNums;
		}
		return 0l;

	}

	public long getAndResetServiceCircuitCount(String serviceId){
		LongAdder longAdder = this.serviceIdCircuitMetricMap.get(serviceId);
		if(longAdder != null){
			long circuitNums = longAdder.sumThenReset();
			return circuitNums;
		}
		return 0l;

	}

	// 获取此App所有serviceId的自定义响应次数
	public long getAppCustomizedResponseHitCount() {
		long appCustomizedResponseHitCount = 0L;
		for (AppRequestFailedCount appRequestFailedCount : appRequestFailedMap.values()) {
			appCustomizedResponseHitCount += appRequestFailedCount.getCustomizedResponseHitCount().get();
		}
		return appCustomizedResponseHitCount;
	}


	public void clearCircuitMetricMap(){
		this.serviceIdCircuitMetricMap.clear();
	}

	public void clearFlowControlMetricMap() {
		this.serviceIdFlowControllerMetricMap.clear();
	}


	public void setServiceIdlatencyPercent(String host, String serviceId, ApiMetricMonitorVO vo){
		ConcurrentHashMap<String,Histogram> hostTP90Map = this.appHostServiceIdTP90Map.get(host);
		if(hostTP90Map != null){
			Histogram histogram = hostTP90Map.get(serviceId);
			if(histogram != null){
				vo.setTp50(histogram.getValueAtPercentile(50));
				vo.setTp90(histogram.getValueAtPercentile(90));
				vo.setTp99(histogram.getValueAtPercentile(99));
				vo.setTp999(histogram.getValueAtPercentile(99.9));
				vo.setTp9999(histogram.getValueAtPercentile(99.99));
				vo.setMax(histogram.getMaxValue());
				vo.setMin(histogram.getMinValue());
				histogram.reset();
			}
		}

	}


	public void addQPS(long qps){
		this.qps += qps;
	}


	public void setName(String name) {
		this.name = name;
		this.contextRoot = Constants.HTTP_SCHEMA + name;
	}

	public void setCheckParameters(Integer checkParameters) {
		this.checkParameters = checkParameters;
	}

	public void setSecurityKey(String securityKey) {
		this.securityKey = securityKey;
	}


	public void setSignatureParameterName(String signatureParameterName) {
		this.signatureParameterName = signatureParameterName;
	}


	public void setPoolGroupType(int poolGroupType) {
		this.poolGroupType = poolGroupType;
	}


	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}


	public void setLoadBalanceName(String loadBalanceName) {
		this.loadBalanceName = loadBalanceName;
	}


	public void setDiscard(Integer discard) {
		this.discard = discard;
	}


	public void setDiscardPath(String discardPath) {
		this.discardPath = discardPath;
	}


	public void setServiceIdCount(int serviceIdCount) {
		this.serviceIdCount = serviceIdCount;
	}

	public void setBlackSelectorIds(String blackSelectorIds) {
		this.blackSelectorIds = blackSelectorIds;
	}


	public void setType(Integer type) {
		this.type = type;
	}


	public ConcurrentHashMap<String,ApiMetric> getHostServiceIdMetricMap(String host){
		return this.appHostServiceIdMetricMap.get(host);
	}

	public ApiMetric getApiMetricByServiceIdAndHost(String serviceId,String host){
		if(StringUtil.isEmptyString(serviceId)||StringUtil.isEmptyString(host)||serviceId.endsWith(Constants.ICO_REQUEST)
				||serviceId.equals(Constants.UNVALID_SERVICE_PATH)){
			return null;
		}
		ConcurrentHashMap<String,ApiMetric> hostMerticMap = this.appHostServiceIdMetricMap.get(host);
		if(hostMerticMap == null){
			hostMerticMap = new ConcurrentHashMap<String, ApiMetric>();
			ConcurrentHashMap<String,ApiMetric> preHostMerticMap = this.appHostServiceIdMetricMap.putIfAbsent(host,hostMerticMap);
			if(preHostMerticMap != null){
				hostMerticMap = preHostMerticMap;
			}
		}


		ApiMetric apiMetric = hostMerticMap.get(serviceId);
		if(apiMetric != null){
			return apiMetric;
		}else{

			apiMetric = new ApiMetric(serviceId);
			ApiMetric preMetric = hostMerticMap.putIfAbsent(serviceId,apiMetric);
			if(preMetric != null){
				return preMetric;
			}else{
				return apiMetric;
			}
		}
	}


	public AppRequestFailedCount getAppRequestFailedCount(String serviceId) {
		if (StringUtil.isEmptyString(serviceId)) {
			return null;
		}

		AppRequestFailedCount appRequestFailedCount = appRequestFailedMap.get(serviceId);
		if (appRequestFailedCount == null) {
			appRequestFailedCount = new AppRequestFailedCount(serviceId);
			AppRequestFailedCount pre = appRequestFailedMap.putIfAbsent(serviceId, appRequestFailedCount);
			if (pre != null) {
				return pre;
			}
		}
		return appRequestFailedCount;

	}

	public BandwidthCount getAppBandwidthCount(String serviceId){
		if(StringUtil.isEmptyString(serviceId) || appBandwidthMap.size() > SERVICE_ID_LIMIT){
			return null;
		}

		BandwidthCount bandwidthCount = appBandwidthMap.get(serviceId);
		if(bandwidthCount == null){
			bandwidthCount = new BandwidthCount();
			BandwidthCount pre = appBandwidthMap.putIfAbsent(serviceId, bandwidthCount);
			if(pre != null){
				return pre;
			}
		}
		return bandwidthCount;
	}


	public void setPreCircuitUpdateTime(long preCircuitUpdateTime) {
		this.preCircuitUpdateTime = preCircuitUpdateTime;
	}


	public void setOutBandWidth(AtomicLong outBandWidth) {
		this.outBandWidth = outBandWidth;
	}


	@Override
	public String toString() {
		return "ServiceApp{" + "id=" + id + ", name='" + name + '\'' + ", contextRoot='" + contextRoot + '\'' + ", flowRuleIds='" + flowRuleIds + '\'' + ", status=" + status + ", addUser='" + addUser + '\'' + ", gmtCreate=" + gmtCreate + ", gmtModify=" + gmtModify + ", checkParameters=" + checkParameters + ", securityKey='" + securityKey + '\'' + ", signatureParameterName='" + signatureParameterName + '\'' + '}';
	}

	public void setSupportSessionLimit(boolean supportSessionLimit) {
		this.supportSessionLimit = supportSessionLimit;
	}

	public void setSupportFilterRequestHeader(boolean supportFilterRequestHeader) {
		this.supportFilterRequestHeader = supportFilterRequestHeader;
	}


	public void setSupportBehaviorSample(boolean supportBehaviorSample) {
		this.supportBehaviorSample = supportBehaviorSample;
	}

	public void clone(ServiceApp appDO){
		this.setName(appDO.getName());
//		if(appDO.getDiscardResponse() != null) {
//			this.setDiscardResponse(appDO.getDiscardResponse());
//		}
		this.setDiscard(appDO.getDiscard());
		this.setDiscardPath(appDO.getDiscardPath());
		this.setPoolGroupType(appDO.getPoolGroupType());
		this.setCheckParameters(appDO.getCheckParameters());
		this.setSignatureParameterName(appDO.getSignatureParameterName());
		this.setSecurityKey(appDO.getSecurityKey());
		this.setAddUser(appDO.getAddUser());
		this.setContextRoot(appDO.getContextRoot());
		this.setFlowRuleIds(appDO.getFlowRuleIds());
		this.setBlackSelectorIds(appDO.getBlackSelectorIds());
		this.setGroupName(appDO.getGroupName());
		this.setGmtModify(appDO.getGmtModify());
		this.setStatus(appDO.getStatus());
	}


	public static class MaslaHistogram extends ConcurrentHistogram {
		/**
		 * 因为histogram.getMaxValue()与histogram.getValueAtPercentile(99.99)的结果是一样的
		 * 这里手动统计max
		 */
		private long maxRecord = 0L;

		public MaslaHistogram(final long highestTrackableValue, final int numberOfSignificantValueDigits) {
			super(highestTrackableValue, numberOfSignificantValueDigits);
		}

		public void recordValue(final long value) throws ArrayIndexOutOfBoundsException {
			super.recordValue(value);
			if (maxRecord < value) {
				maxRecord = value;
			}
		}

		public long getMaxValue() {
			long maxhistogram = super.getMaxValue();
			return Math.max(maxRecord, maxhistogram);
		}

		public void reset() {
			super.reset();
			maxRecord = 0L;
		}

	}

}
