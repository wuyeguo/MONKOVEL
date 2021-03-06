package com.monke.monkeybook.model.content;

import android.util.Log;

import com.monke.monkeybook.help.FormatWebText;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.text.TextUtils.isEmpty;

/**
 * Created by GKF on 2018/1/25.
 * 书源规则解析
 */

public class AnalyzeRule {
    private String baseURI;
    private Element element;

    public AnalyzeRule(Element element, String baseURI) {
        this.element = element;
        this.baseURI = baseURI;
    }

    public static Elements getElements(Element temp, String rule) {
        Elements elements = new Elements();
        if (temp == null || isEmpty(rule)) {
            return elements;
        }
        try {
            String[] rs = rule.split("@");
            if (rs.length > 1) {
                elements.add(temp);
                for (String rl : rs) {
                    Elements es = new Elements();
                    for (Element et : elements) {
                        es.addAll(getElements(et, rl));
                    }
                    elements.clear();
                    elements.addAll(es);
                }
            } else {
                String[] rulePc = rule.split("!");
                String[] rules = rulePc[0].split("\\.");
                switch (rules[0]) {
                    case "class":
                        if (rules.length == 3) {
                            elements.add(temp.getElementsByClass(rules[1]).get(Integer.parseInt(rules[2])));
                        } else {
                            elements.addAll(temp.getElementsByClass(rules[1]));
                        }
                        break;
                    case "tag":
                        if (rules.length == 3) {
                            elements.add(temp.getElementsByTag(rules[1]).get(Integer.parseInt(rules[2])));
                        } else {
                            elements.addAll(temp.getElementsByTag(rules[1]));
                        }
                        break;
                    case "id":
                        elements.add(temp.getElementById(rules[1]));
                        break;
                }
                if (rulePc.length > 1) {
                    String[] rulePcs = rulePc[1].split(":");
                    for (String pc : rulePcs) {
                        if (pc.equals("%")) {
                            elements.remove(elements.last());
                        } else {
                            elements.remove(elements.get(Integer.parseInt(pc)));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("getElements", e.getMessage());
        }
        return  elements;
    }

    public String getResult(String ruleStr) {
        if (isEmpty(ruleStr)) {
            return null;
        }
        List<String> textS = getResultList(ruleStr);
        if (textS == null) {
            return null;
        }
        StringBuilder content = new StringBuilder();
        for (String text : textS) {
            text = FormatWebText.getContent(text);
            if (textS.size() > 1) {
                if (text.length() > 0) {
                    if (content.length() > 0) {
                        content.append("\r\n");
                    }
                    content.append("\u3000\u3000").append(text);
                }
            } else {
                content.append(text);
            }
        }
        return content.toString();
    }

    public List<String> getResultList(String ruleStr) {
        if (isEmpty(ruleStr)) {
            return null;
        }
        Elements elements = new Elements();
        elements.add(element);
        String[] rules = ruleStr.split("@");
        for (int i = 0; i < rules.length - 1; i++) {
            Elements es = new Elements();
            for (Element elt : elements) {
                es.addAll(getElements(elt, rules[i]));
            }
            elements.clear();
            elements = es;
        }
        if (elements.isEmpty()) {
            return null;
        }
        try {
            List<String> textS = new ArrayList<>();
            String lastRule = rules[rules.length - 1];
            switch (lastRule) {
                case "text":
                    for (Element element : elements) {
                        textS.add(element.text());
                    }
                    break;
                case "textNodes":
                    List<TextNode> contentEs = elements.get(0).textNodes();
                    for (int i = 0; i < contentEs.size(); i++) {
                        String temp = contentEs.get(i).text().trim();
                        temp = FormatWebText.getContent(temp);
                        if (temp.length() > 0) {
                            textS.add(temp);
                        }
                    }
                    break;
                default:
                    String absURL = getAbsoluteURL(baseURI, elements.get(0).attr(lastRule));
                    textS.add(absURL);
            }
            return textS;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("getResultList", e.getMessage());
            return null;
        }
    }

    /**
     * 获取绝对地址
     */
    private static String getAbsoluteURL(String baseURI, String relativePath){
        String abURL=relativePath;
        try {
            URI base=new URI(baseURI);//基本网页URI
            URI abs=base.resolve(relativePath);//解析于上述网页的相对URL，得到绝对URI
            URL absURL=abs.toURL();//转成URL
            abURL = absURL.toString();
        } catch (MalformedURLException | URISyntaxException e) {
            e.printStackTrace();
        }
        return abURL;
    }

}

