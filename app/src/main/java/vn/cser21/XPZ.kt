package vn.cser21

import android.R
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper

import android.util.Base64
import net.posprinter.IConnectListener
import net.posprinter.IDeviceConnection
import net.posprinter.POSConnect
import net.posprinter.POSPrinter
import net.posprinter.model.PTable
import java.io.File


class XPZ {
    var app21: App21? = null;
    var result: Result? = null;

    private var printer: POSPrinter? = null
    private var curConnect: IDeviceConnection? = null

    private var inited: Boolean = false;
    private var connectStatus: Int = 0;
    private var ipConnected: String? = null;
    private var _ipAddress: String? = null;

    private var width: Int = 320;
    private var printBmp: Bitmap? = null;
    private var printText: String? = null;
    private var printParam: XPZParam? = null;

    private var resourse: List<String?> = ArrayList()
    private  var printed: Boolean = false;
    private  var disconnectN: Int = 0;
    private fun reset() {
        printer = null;
        curConnect = null;
        connectStatus = 0;
        ipConnected = null;
    }

    private fun response(success: Boolean, dataText: String?) {
        result?.success = success;
        result?.data = dataText;
        app21?.App21Result(result);

    }

    private val connectListener = IConnectListener { code, connInfo, msg ->
        when (code) {
            POSConnect.CONNECT_SUCCESS -> {
//                UIUtils.toast(R.string.con_success)
//                LiveEventBus.get<Boolean>(Constant.EVENT_CONNECT_STATUS).post(true)
                //response(true, null);
                connectStatus = 200;
                ipConnected = _ipAddress;
                if (printParam != null) {
                    doPrint();
                } else if (printBmp != null) {
                    doPrint();
                } else if (printText != null) {
                    doPrint();
                } else {
                    response(true, "CONNECT_SUCCESS:${msg}");
                }
            }


            POSConnect.CONNECT_FAIL -> {
//                UIUtils.toast(R.string.con_failed)
//                LiveEventBus.get<Boolean>(Constant.EVENT_CONNECT_STATUS).post(false)
                reset();
                response(false, "CONNECT_FAIL:${msg}");
            }

            POSConnect.CONNECT_INTERRUPT -> {
//                UIUtils.toast(R.string.con_has_disconnect)
//                LiveEventBus.get<Boolean>(Constant.EVENT_CONNECT_STATUS).post(false)
                reset()
                response(false, "CONNECT_INTERRUPT:${msg}");
            }

            POSConnect.SEND_FAIL -> {
                //UIUtils.toast(R.string.send_failed)
                response(false, "SEND_FAIL:${msg}");
            }

            POSConnect.USB_DETACHED -> {
                //UIUtils.toast(R.string.usb_detached)
            }

            POSConnect.USB_ATTACHED -> {
                //UIUtils.toast(R.string.usb_attached)
            }

        }
    }


    fun connectNet(ipAddress: String) {

        if (!inited) {
            inited = true;
            POSConnect.init(app21?.mContext)
        }

        _ipAddress = ipAddress;
        if (ipAddress != ipConnected) {
            connectStatus = 0;
        }

        if (connectStatus != 0) return;
        connectStatus = 1;
        try {
            curConnect?.close()
            curConnect = POSConnect.createDevice(POSConnect.DEVICE_TYPE_ETHERNET)
            curConnect!!.connect(ipAddress, connectListener);
            ipConnected = ipAddress;

            //check time out
            checkTimeout()
        } catch (e: Exception) {
            response(false, e.message);
        }
    }

    private  fun  checkTimeout(){
        Handler(Looper.getMainLooper()).postDelayed({

            if(connectStatus != 200 && !printed)
            {
                reset();
                response(false, "timeout");
            }

        }, 5000)
    }




    private fun let(value: Int?): Int {
        var v: Int = 0;
        value?.let {
            v = value!!;
        }
        return v
    }

    private fun doPrint() {

        try {
            if (printer == null) {
                printer = POSPrinter(curConnect);
            }

            val p = printer;



            if (printParam != null) {
                p?.initializePrinter();
                if (printParam?.items != null) {
                    for (item in printParam?.items!!) {


                        if (item.pdfLink != null) {
                            p?.printPDF417(
                                item.pdfString,
                                item.cellWidth,
                                item.cellHeightRatio,
                                item.numberOfColumns,
                                item.numberOfRows,
                                item.eclType,
                                item.eclValue,
                                item.alignment
                            )

                        }

                        if (item.text != null) {
                            if (item.alignment > 0 || item.attribute > 0 || item.textSize > 0) {
                                p?.printText(
                                    item.text, item.alignment, item.attribute, item.textSize
                                );
                            } else {
                                p?.printString(item.text);
                            }

                        }

                        if (item.barCode != null) {
                            p?.printBarCode(
                                item.barCode,
                                item.codeType,
                                item.width,
                                item.height,
                                item.alignment,
                                item.textPosition
                            )
                        }

                        if (item.qrCode != null) {
                            p?.printQRCode(
                                item.qrCode,
                                item.moduleSize,
                                item.ecLevel,
                                item.alignment
                            )
                        }

                        if (item.bitmapPath != null) {
                            p?.printBitmap(
                                item.bitmapPath,
                                item.alignment,
                                item.width,
                                item.model
                            )
                        }

                        if (item.base64 != null) {



                            val decodedString: ByteArray =
                                Base64.decode(item.base64, Base64.DEFAULT)
                            val bitmap64 =
                                BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                            p?.printBitmap(bitmap64, item.alignment, item.width, item.model)
                        }

                        if (item.imageLocalPath != null) {


                            val dn = DownloadFilesTask();
                            dn.app21 = app21;

                            val bitmapUrl = dn.getBitmap(item.imageLocalPath);
                            p?.printBitmap(bitmapUrl, item.alignment, item.width, item.model)

                            resourse += item.imageLocalPath!!;
                        }

                        if (item.table != null) {
                            val tb = item.table!!;
                            val table = PTable(tb.titles, tb.bytesPerCol, tb.align);
                            for (row in tb.rows!!) {
                                table.addRow(null, row);
                            }

                            p?.printTable(table);
                        }

                        //item.deleteFilesIfHas();
                    }
                }

                if (printParam!!.feedLine) {
                    p?.feedLine();
                }
                if (printParam!!.cutHalfAndFeed > 0) {
                    p?.cutHalfAndFeed(printParam!!.cutHalfAndFeed);
                }

                if (printParam!!.cutPaper) {
                    p?.cutPaper();
                }


            }
            response(true, dataText = "done");
        } catch (e: Exception) {
            response(false, e.message);
        }
        if(printParam != null && printParam?.keepAlive != true)
        {
            disconnectN += 1;
            disconnect();
        }
        printed = true;

    }



    public fun printParam(ipAddress: String, param: XPZParam?) {
        try {
            this.printParam = param;
            this.disconnectN = 0;
            this.printed = false;
            if (connectStatus == 200 && ipAddress == ipConnected) {
                doPrint();
            } else {
                connectNet(ipAddress);
            }
        } catch (e: Exception) {
            response(false, e.message);
        }
    }

    public  fun disconnect(){


        Handler(Looper.getMainLooper()).postDelayed({

            clear();
            if(connectStatus ==200 && disconnectN> 0)
            {
                try {
                    curConnect?.close()
                }catch (e: Exception){

                }
            }
            reset();

        }, 5000)
    }

    public  fun clear(){

        for(lc in resourse)
        {
            try {
                val file = lc?.let { File(it) };
                if (file != null) {
                    if(file.exists()) {
                        file.delete()
                    }
                }
            }catch (_: Exception){

            }
        }


        resourse = ArrayList();
    }
}

class XPZItem {
    //pdf
    var pdfLink: String? = null;
    var pdfString: String? = null;
    var cellWidth: Int = 0;
    var cellHeightRatio: Int = 0
    var numberOfColumns: Int = 0
    var numberOfRows: Int = 0
    var eclType: Int = 0
    var eclValue: Int = 0

    //text
    var text: String? = null;
    var attribute: Int = 0
    var textSize: Int = 0

    //barcode
    var barCode: String? = null;
    var codeType: Int = 0
    var height: Int = 0
    var textPosition: Int = 0

    //qrcode
    var qrCode: String? = null;
    var moduleSize: Int = 0
    var ecLevel: Int = 0

    //bitmap
    var bitmapPath: String? = null;
    var model: Int = 0

    var base64: String? = null;
    var imageUrl: String? = null;
    var imageLocalPath: String? = null;

    //table
    var table: XPZTable? = null;

    //pdf,text, barcode, qrCode, bitmap
    var alignment: Int = 0

    //barcode, bitmap
    var width: Int = 0

    public fun deleteFilesIfHas() {

        if (imageLocalPath != null) {
            
            imageLocalPath?.let { 
               val  fdelete = File(it) ;
                if(fdelete.exists())
                {
                    fdelete.delete()    
                }
            }
        }
        //*/
    }
}

class XPZParam {
    public var items: List<XPZItem>? = null;
    var feedLine: Boolean = true;
    var cutHalfAndFeed: Int = 1;
    var cutPaper: Boolean = true;
    var keepAlive: Boolean = false;
}

class XPZTable {
    var titles: Array<String>? = null;
    var bytesPerCol: Array<Int>? = null;
    var align: Array<Int>? = null;
    var rows: ArrayList<Array<String>>? = null;

}