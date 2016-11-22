package vincent.intelligentinstaller;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by vincent on 2016/11/21.
 * email-address:674928145@qq.com
 * description:该ApkService会检测com.android.packageinstaller而隐式启动
 */

public class ApkService extends AccessibilityService {
    HashMap<Integer, Boolean> hashMap = new HashMap<>();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        AccessibilityNodeInfo nodeInfo = accessibilityEvent.getSource();
        if (nodeInfo != null) {
            if (hashMap.get(accessibilityEvent.getWindowId()) == null) {
                boolean handle = interNodeInfo(nodeInfo);
                if (handle) {
                    //记录了当前已经点击了的windowId
                    hashMap.put(accessibilityEvent.getWindowId(), handle);
                }
            }
        }

    }

    private boolean interNodeInfo(AccessibilityNodeInfo nodeInfo) {
        int childCount = nodeInfo.getChildCount();
        //魅族安装，完成等按钮用的是TextView
        if (nodeInfo.getClassName().equals("android.widget.Button") || nodeInfo.getClassName().equals("android.widget.TextView")) {
            String nodeContent = nodeInfo.getText().toString();
            //找到相应的安装按钮
            if ("安装".equals(nodeContent) || "完成".equals(nodeContent) || "确定".equals(nodeContent) || "下一步".equals(nodeContent) || "继续".equals(nodeContent)) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                return true;
            }
        } //如果包含了一个ScrollView，把该View往前滚，以确保能遍历到确认的按钮
        else if ("android.widget.ScrollView".equals(nodeInfo.getClassName())) {
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
        }

        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo child = nodeInfo.getChild(i);
            if (interNodeInfo(child)) {//递归调用，找到相应的安装按钮
                return true;
            }
        }
        return false;
    }

    @Override
    public void onInterrupt() {

    }
}
