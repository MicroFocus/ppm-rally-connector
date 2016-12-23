package com.hp.ppm.integration.rally;

import java.util.Arrays;
import java.util.List;

import com.hp.ppm.integration.FunctionIntegration;
import com.hp.ppm.integration.IntegrationConnector;
import com.hp.ppm.integration.IntegrationConnectorInstance;
import com.hp.ppm.integration.ui.Field;
import com.hp.ppm.integration.ui.PlainText;

public class RallyIntegrationConnector implements IntegrationConnector {

	@Override
	public String getTargetApplication() {
		return "Rally";
	}

	@Override
	public String getTargetApplicationVersion() {
		return "2.0";
	}

	@Override
	public String getTargetApplicationIcon(){
		return "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAACSUlEQVQ4jZWTS0hVURSGv3X2Pt6Hgl20B2aiUqmpPUAyiKS0KDJqYEEDqUFh0CSKHAVNCqKJg2YFkQ16EPQYNCsKggIFSwMle2E5ESrqekPPPZ5zdoN9uV6piP7Rfq1/rf9fawsWLqD4NxQQ5tZzQCiA27u2/m7bkZ5V/oM7GD8LIvZJFIEx6O2dsKIKMzRA9HEcN+vT+2qsfzwzc1EDamtNTcPujvaVDDzBGDOfLwiRYycgHofrVzDfv0HZIsTVXHrzoWk8M4MGyBIFBCFzYQgICODNovZ3QyxG0HcOVBFobfVGEaGxUhwA43mgBHGL8qXLsgqcllaC/ss2WDl/NMUS/Jy2esvKIQrB93E2biZ6Nw5fpv4anCfAz8LUFE5DM8z5gEGqazFvx/Jl5xFFEASQ88oSOBoGnyMtm8BxbBfiCchkQHLZjYGsh5Qvgab1UFxSQOBqzOuXIKBat2C8WfCySHEJmAiMzay7j6KPn4L2XcjipQUEOYT3biP7DiClKczEe1hdb8v1PdSeLqioJLhwFvrO2/tCAtEaMzqCGXyBe/IM0dAAzso6JFUGbhGyoYXw5jVMdtbORU7aQntjCcL7tzCTE6hDPZjJT6iDh60nxsB0GtRCU/M7A3aAiuIEN66idu5Ftu1AqmvRUQSeh9O4jvDZY2ugieYJSrWKiVa4oT0kkYRHD2FsBDq7kLYOSMSRxmacVAo+T+AOjmq+phFA1ZUkTy9PxNcYCj9CQc9LUziVVZhkEkQQ35eh4eGnP9Lp/t8m63/xC/QO18wxC1a8AAAAAElFTkSuQmCC";
	}

	@Override
	public List<Field> getDriverConfigurationFields() {
		return Arrays.asList(new Field[] {
			new PlainText(Constants.KEY_BASE_URL, "BASE_URL", "", true),
			new PlainText(Constants.KEY_PROXY_HOST,"PROXY_HOST","",false),
			new PlainText(Constants.KEY_PROXY_PORT,"PROXY_PORT","",false)
		});
	}

	@Override
	public List<FunctionIntegration> getIntegrations() {
		return Arrays.asList(new FunctionIntegration[]{
			new RallyWorkPlanIntegration()
		});
	}

	@Override
	public IntegrationConnectorInstance getBuildinInstance() {
		return null;
	}
}
