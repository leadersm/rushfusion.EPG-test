package com.rushfusion.test.epg;

public class ChannelInfo {
	private int frequency = 0;
	private int modulation = 0;
	private int originalNetworkId = 1;
	private int pmtPid = 0;
	private int programNumber = 1001;
	private String serviceName = null;
	private int symbolRate = 0;
	private int transportStreamId = 1;

	public int getFrequency() {
		return this.frequency;
	}

	public int getModulation() {
		return this.modulation;
	}

	public int getOriginalNetworkId() {
		return this.originalNetworkId;
	}

	public int getPmtPid() {
		return this.pmtPid;
	}

	public int getProgramNumber() {
		return this.programNumber;
	}

	public String getServiceName() {
		return this.serviceName;
	}

	public int getSymbolRate() {
		return this.symbolRate;
	}

	public int getTransportStreamId() {
		return this.transportStreamId;
	}

	public void setFrequency(int paramInt) {
		this.frequency = paramInt;
	}

	public void setModulation(int paramInt) {
		this.modulation = paramInt;
	}

	public void setOriginalNetworkId(int paramInt) {
		this.originalNetworkId = paramInt;
	}

	public void setPmtPid(int paramInt) {
		this.pmtPid = paramInt;
	}

	public void setProgramNumber(int paramInt) {
		this.programNumber = paramInt;
	}

	public void setServiceName(String paramString) {
		this.serviceName = paramString;
	}

	public void setSymbolRate(int paramInt) {
		this.symbolRate = paramInt;
	}

	public void setTransportStreamId(int paramInt) {
		this.transportStreamId = paramInt;
	}

	@Override
	public String toString() {
		return "ChannelInfo [frequency=" + frequency + ", modulation="
				+ modulation + ", originalNetworkId=" + originalNetworkId
				+ ", pmtPid=" + pmtPid + ", programNumber=" + programNumber
				+ ", serviceName=" + serviceName + ", symbolRate=" + symbolRate
				+ ", transportStreamId=" + transportStreamId + "]";
	}

}