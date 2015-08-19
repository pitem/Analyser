/**
 * @author yuanhh1
 * 
 * @email yuanhh1@lenovo.com
 * 
 */
package com.yhh.analyser.bean;

import com.yhh.analyser.bean.app.PhoneInfo;
import com.yhh.analyser.utils.CommandUtils;
import com.yhh.analyser.utils.ConstUtils;
import com.yhh.analyser.utils.FileUtils;

class ScreenInfo {
    
	public float getBrightness() {
	    float brightness = 0;
	    String cmd =null;
	    
	    if(PhoneInfo.getBrand().contains(ConstUtils.BRAND_HUAWEI)){
	        cmd = CommandUtils.CMD_BRIGHTNESS_NOW_HUAWEI;
	    }else{
	        cmd = CommandUtils.CMD_BRIGHTNESS_NOW;
	    }
	    
        String str = FileUtils.getCommandNodeValue(cmd);
        if(str !=null && !"".equals(str)){
            brightness =  Integer.valueOf(str.trim());
        }
		return brightness;
	}
	
}