package com.fredx;

/**
 * 事件回调操作
 * 描述：模拟界面类  接收用户发起的事件，处理完成，返回结果
 * 1、下载完成后，需要显示信息
 * 2、下载过程中，需要显示下载进度
 *
 * 代码某处，该做何事，以及该事怎么做 ----》 在此处直接调用一个函数就行
 *
 */
public class GuiTestCase {
    private DownLoad downLoad;
    public GuiTestCase(){
        this.downLoad = new DownLoad(this);
    }
    /**
     * 下载文件
     * @param file
     */
    public void downLoadFile(String file){
        System.out.println("begin start file:" + file);
        downLoad.start(file);
    }

    /**
     * 显示下载进度
     * @param file
     * @param progress
     */
    public void progress(String file, int progress){
        System.out.println("download file:" + file + "progress" + progress + "%");
    }

    public void result(String file){
        System.out.println("download file:" + file + "over.");
    }

    public static void main(String[] args) {
        GuiTestCase gui = new GuiTestCase();
        gui.downLoadFile("learning java");
    }

}

/**
 * 负责下载内容的类
 */
class DownLoad{
    private GuiTestCase gui;
    public DownLoad(GuiTestCase gui){  //没有做到面向接口的编程，因为底层和上层互相强依赖
        this.gui = gui;
    }
    /**
     * 底层执行下载任务的方法
     * @param file
     */
    public void start(String file){
        int count = 0;
        try {
            while (count <= 100){
                gui.progress(file, count);
                Thread.sleep(1000);
                count += 20;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        gui.result(file);
    }
}
