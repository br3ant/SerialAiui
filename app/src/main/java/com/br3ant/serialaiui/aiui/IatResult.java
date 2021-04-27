package com.br3ant.serialaiui.aiui;

import java.util.List;

/**
 * @author Richie on 2019.05.17
 */
public class IatResult {

    /**
     * sn : 1
     * ls : true
     * bg : 0
     * ed : 0
     * ws : [{"bg":0,"cw":[{"sc":0,"w":"来"}]},{"bg":0,"cw":[{"sc":0,"w":"一"}]},{"bg":0,"cw":[{"sc":0,"w":"首"}]},{"bg":0,"cw":[{"sc":0,"w":"音乐"}]},{"bg":0,"cw":[{"sc":0,"w":""}]}]
     */

    private int sn;
    private boolean ls;
    private int bg;
    private int ed;
    private List<WsBean> ws;

    public String getContent() {
        if (ws != null) {
            StringBuilder sb = new StringBuilder();
            for (WsBean w : ws) {
                List<WsBean.CwBean> cw = w.getCw();
                if (cw != null) {
                    for (WsBean.CwBean cwBean : cw) {
                        sb.append(cwBean.getW());
                    }
                }
            }
            return sb.toString();
        }
        return "";
    }

    public int getSn() {
        return sn;
    }

    public void setSn(int sn) {
        this.sn = sn;
    }

    public boolean isLs() {
        return ls;
    }

    public void setLs(boolean ls) {
        this.ls = ls;
    }

    public int getBg() {
        return bg;
    }

    public void setBg(int bg) {
        this.bg = bg;
    }

    public int getEd() {
        return ed;
    }

    public void setEd(int ed) {
        this.ed = ed;
    }

    public List<WsBean> getWs() {
        return ws;
    }

    public void setWs(List<WsBean> ws) {
        this.ws = ws;
    }

    @Override
    public String toString() {
        return "IatResult{" +
                "sn=" + sn +
                ", ls=" + ls +
                ", bg=" + bg +
                ", ed=" + ed +
                ", ws=" + ws +
                '}';
    }

    public static class WsBean {
        /**
         * bg : 0
         * cw : [{"sc":0,"w":"来"}]
         */

        private int bg;
        private List<CwBean> cw;

        public int getBg() {
            return bg;
        }

        public void setBg(int bg) {
            this.bg = bg;
        }

        public List<CwBean> getCw() {
            return cw;
        }

        public void setCw(List<CwBean> cw) {
            this.cw = cw;
        }

        @Override
        public String toString() {
            return "WsBean{" +
                    "bg=" + bg +
                    ", cw=" + cw +
                    '}';
        }

        public static class CwBean {
            /**
             * sc : 0
             * w : 来
             */

            private int sc;
            private String w;

            public int getSc() {
                return sc;
            }

            public void setSc(int sc) {
                this.sc = sc;
            }

            public String getW() {
                return w;
            }

            public void setW(String w) {
                this.w = w;
            }

            @Override
            public String toString() {
                return "CwBean{" +
                        "sc=" + sc +
                        ", w='" + w + '\'' +
                        '}';
            }
        }
    }
}
