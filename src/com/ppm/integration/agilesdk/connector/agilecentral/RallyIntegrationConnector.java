package com.ppm.integration.agilesdk.connector.agilecentral;

import com.ppm.integration.agilesdk.FunctionIntegration;
import com.ppm.integration.agilesdk.IntegrationConnector;
import com.ppm.integration.agilesdk.ui.Field;
import com.ppm.integration.agilesdk.ui.PlainText;

import java.util.Arrays;
import java.util.List;

public class RallyIntegrationConnector extends IntegrationConnector {

    @Override public String getExternalApplicationName() {
        return "CA Central Central (Rally)";
    }

    @Override public String getConnectorVersion() {
        return "2.0";
    }

    @Override public String getTargetApplicationIcon() {
        return "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsQAAA7EAZUrDhsAAAH5SURBVDhPpZO/q/lRGMffvsNFNgbqKhMyIIvBeiVls8iPMlyLlJT8B2SR7sRm8COjyY9SuozqDiRlMShCUUrCjXM/5/j4dGX6dl/1dJ7zPs/zfM55zvmICAf+wEOBfD6PwWAAsVgMl8sFh8OBXq+HRqOB7XYLuVwOv98Po9HIZ3DQAtPplBZ5MpvNRtRq9ZOeTqdpGoPtQCQScfqNYrGI1WqFRCLB5l6vFyaTCVqtFj6fD+fzmelcGhvRbDaFyplMhtZjJJNJEgqFSKVS4RVCotGoEHsHqVRKEDudDi/fmEwmhOuFsP7b7vy7XC7c/Mb39zfvAdVqFXq9HvV6HW63G19fXwiHw/zqL2q1mlA1FovxdQnRaDRMCwQCvELI+/v70w6YdxepBYNBEolEiNlsZnOdTkd2ux1pt9sPcXeYNxwOHxapcd1/0qxWq+DPZjPCHet2jZzAKBQKGI1GUCgU7MpeXl6Qy+XoR+DxeGAwGFAul1ms0+kEvf6/P2XaJNplpVKJxWKBUqmEeDyOfr8Pi8WC5XKJ9XrNdiWTyZj/+voKiUSC+XwOUTabJdxjgkqlYtum/wI1mjwej1kgfX1SqRTcuWG329FqtfD29oZutwscDgfCBdCTkM1mw8Y71+uVGeV0OpGPjw/y+fnJ5sfjkez3+8cm/j/ADyGll4fUyTxIAAAAAElFTkSuQmCC";
    }

    @Override public List<Field> getDriverConfigurationFields() {
        return Arrays.asList(new Field[] {
                new PlainText(Constants.KEY_BASE_URL, "BASE_URL", "https://demo-rally.rallydev.com", true),
                new PlainText(Constants.KEY_PROXY_HOST, "PROXY_HOST", "", false),
                new PlainText(Constants.KEY_PROXY_PORT, "PROXY_PORT", "", false)});
    }

    @Override public List<FunctionIntegration> getIntegrations() {
        return Arrays
                .asList(new FunctionIntegration[] {new RallyWorkPlanIntegration(), new RallyTimeSheetIntegration()});
    }

}
