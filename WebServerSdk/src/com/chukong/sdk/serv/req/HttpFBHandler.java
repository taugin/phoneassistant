package com.chukong.sdk.serv.req;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.RequestLine;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.chukong.sdk.Constants.Config;
import com.chukong.sdk.GlobalInit;
import com.chukong.sdk.common.Log;
import com.chukong.sdk.serv.req.objs.FileRow;
import com.chukong.sdk.serv.req.objs.TwoColumn;
import com.chukong.sdk.serv.support.Progress;
import com.chukong.sdk.serv.view.ViewFactory;
import com.chukong.sdk.util.CommonUtil;
import com.google.gson.Gson;

/**
 * @brief 目录浏览页面请求处理
 * @author join
 */
public class HttpFBHandler implements HttpRequestHandler {

    private static final String TAG_DOWNLOAD = "/download";
    private static final String TAG_APPFILE = "/appfile";
    private static final String TAG_APPLIST = "/applist";
    private CommonUtil mCommonUtil = CommonUtil.getSingleton();
    private ViewFactory mViewFactory = ViewFactory.getSingleton();

    private String webRoot;

    public HttpFBHandler(final String webRoot) {
        this.webRoot = webRoot;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response,
            HttpContext context) throws HttpException, IOException {
        //printRequest(request);
        String target = URLDecoder.decode(request.getRequestLine().getUri(),
                Config.ENCODING);
        Header requestHost = request.getFirstHeader("Host");
        InetAddress ipAddress = (InetAddress) context
                .getAttribute("remote_ip_address");
        Log.d(Log.TAG, "requestHost = " + requestHost);
        Log.d(Log.TAG, "clientIpAddress = " + ipAddress.getHostAddress()
                + " , target = " + target);
        String requestMethod = null;
        RequestLine requestLine = request.getRequestLine();
        if (requestLine != null) {
            requestMethod = requestLine.getMethod();
        }
        String hostAddress = requestHost != null ? requestHost.getValue()
                : null;
        if (target.startsWith(TAG_DOWNLOAD)) {
            target = target.substring(TAG_DOWNLOAD.length());
            redirectToDownload(target, hostAddress, response);
            return ;
        }

        File file = null;
        // Handle the app files
        Log.d(Log.TAG, "webRoot = " + webRoot);
        if (target.equals(TAG_APPLIST)) {
            file = new File(webRoot);
            processFile(file, request, response);
            return;
        }
        // Handle the css/js files
        Log.d(Log.TAG, "SERV_ROOT_DIR = " + Config.SERV_ROOT_DIR);
        if (target.startsWith(Config.SERV_ROOT_DIR)){
            file = new File(target);
            processFile(file, request, response);
            return ;
        }
        // Handle the app download
        if (target.startsWith(TAG_APPFILE)) {
            target = target.substring(TAG_APPFILE.length());
            file = new File(target);
            processDownloadFile(file, request, response);
            return ;
        }
        //Others redirect to proper location
        redirectToView(request, response,
                hostAddress, requestMethod, requestHost);
        Progress.clear();
    }

    private void processFile(File file, HttpRequest request, HttpResponse response)
            throws HttpException, IOException {
        Log.d(Log.TAG, "file = " + file);
        HttpEntity entity = null;
        String contentType = "text/html;charset=" + Config.ENCODING;
        if (file != null) {
            if (!file.exists()) { // 不存在
                response.setStatusCode(HttpStatus.SC_NOT_FOUND);
                entity = resp404(request);
            } else if (file.canRead()) { // 可读
                response.setStatusCode(HttpStatus.SC_OK);
                if (file.isDirectory()) {
                    entity = respView(request, file);
                } else {
                    entity = respFile(request, file);
                    contentType = entity.getContentType().getValue();
                }
            } else { // 不可读
                response.setStatusCode(HttpStatus.SC_FORBIDDEN);
                entity = resp403(request);
            }
        }

        response.setHeader("Content-Type", contentType);
        response.setEntity(entity);
        // Log.d(Log.TAG, "contentType = " + contentType);
        //printResponse(response);
        Progress.clear();
    }
    private void processDownloadFile(File file, HttpRequest request, HttpResponse response)
            throws HttpException, IOException {
        Log.d(Log.TAG, "file = " + file);
        HttpEntity entity = null;
        String downloadName = null;
        String contentType = "text/html;charset=" + Config.ENCODING;
        if (file != null) {
            if (!file.exists()) { // 不存在
                response.setStatusCode(HttpStatus.SC_NOT_FOUND);
                entity = resp404(request);
            } else if (file.canRead()) { // 可读
                response.setStatusCode(HttpStatus.SC_OK);
                if (file.isDirectory()) {
                    entity = respView(request, file);
                } else {
                    entity = respFile(request, file);
                    downloadName = getApkName(file.getAbsolutePath());
                    contentType = entity.getContentType().getValue();
                }
            } else { // 不可读
                response.setStatusCode(HttpStatus.SC_FORBIDDEN);
                entity = resp403(request);
            }
        }

        Log.d(Log.TAG, "downloadName = " + downloadName);
        if (!TextUtils.isEmpty(downloadName)) {
            String filePath = file.getAbsolutePath();
            String ext = MimeTypeMap.getFileExtensionFromUrl(filePath);
            String urlEncodedName = URLEncoder.encode(downloadName, "utf-8");
            urlEncodedName += ".";
            urlEncodedName += ext;
            String value = "attatchment;filename=" + urlEncodedName;
            response.setHeader("Content-Disposition", value);
        }
        response.setHeader("Content-Type", contentType);
        response.setEntity(entity);
        // Log.d(Log.TAG, "contentType = " + contentType);
        //printResponse(response);
        Progress.clear();
    }

    private HttpEntity respFile(HttpRequest request, File file)
            throws IOException {
        return mViewFactory.renderFile(request, file);
    }

    private HttpEntity resp403(HttpRequest request) throws IOException {
        return mViewFactory.renderTemp(request, "403.html");
    }

    private HttpEntity resp404(HttpRequest request) throws IOException {
        return mViewFactory.renderTemp(request, "404.html");
    }

    private HttpEntity respView(HttpRequest request, File dir)
            throws IOException {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("dirpath", dir.getPath()); // 目录路径
        data.put("hasParent", !isSamePath(dir.getPath(), this.webRoot)); // 是否有上级目录
        List<FileRow> fileRows = buildFileRows(dir);
        data.put("fileRows", fileRows); // 文件行信息集合
        data.put("rowsCount", fileRows.size());
        return mViewFactory.renderTemp(request, "view.html", data);
    }

    private boolean isSamePath(String a, String b) {
        String left = a.substring(b.length(), a.length()); // a以b开头
        if (left.length() >= 2) {
            return false;
        }
        if (left.length() == 1 && !left.equals("/")) {
            return false;
        }
        return true;
    }

    private List<TwoColumn> buildTwoColumn(File dir) {
        List<FileRow> fileRows = buildFileRows(dir);
        if (fileRows == null) {
            return null;
        }
        int index = 0;
        int len = fileRows.size();
        List<TwoColumn> twoColumns = new ArrayList<TwoColumn>();
        TwoColumn twoColumn = null;
        for (index = 0; index < len; index += 2) {
            twoColumn = new TwoColumn();
            if (index < len) {
                twoColumn.fileRow1 = fileRows.get(index);
            }
            if (index + 1 < len) {
                twoColumn.fileRow2 = fileRows.get(index + 1);
            }
            twoColumns.add(twoColumn);
        }
        return twoColumns;
    }

    private File[] getFileFromDataApp() {
        Context context = GlobalInit.getInstance().getBaseContext();
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return null;
        }
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);
        if (apps == null) {
            return null;
        }
        List<File> appFiles = new ArrayList<File>();
        File file = null;
        for (ApplicationInfo info : apps) {
            if (filterPackage(info)) {
                continue;
            }
            file = new File(info.publicSourceDir);
            appFiles.add(file);
        }
        return appFiles.toArray(new File[appFiles.size()]);
    }

    private boolean filterPackage(ApplicationInfo filterInfo) {
        if (filterInfo == null) {
            return true;
        }
        /*
        if ((filterInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
            return true;
        }*/
        if (filterInfo.publicSourceDir.startsWith("/system")) {
            return true;
        }
        String packageName = filterInfo.packageName;
        Context context = GlobalInit.getInstance().getBaseContext();
        PackageManager pm = context.getPackageManager();
        String thisPackage = null;
        if (pm != null) {
            ApplicationInfo info = null;
            try {
                info = pm.getApplicationInfo(context.getPackageName(), 0);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            if (info != null) {
                thisPackage = info.packageName;
            }
        }
        if ("com.gtja.dzh".equals(packageName)
                || "com.hexin.plat.android".equals(packageName)
                || "com.eastmoney.android.berlin".equals(packageName)) {
            return true;
        }
        if (thisPackage != null && thisPackage.equals(packageName)) {
            return true;
        }
        return false;
    }

    private File getSelfAppFile() {
        Context context = GlobalInit.getInstance().getBaseContext();
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return null;
        }
        ApplicationInfo info = null;
        try {
            info = pm.getApplicationInfo(context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        if (info != null && info.publicSourceDir != null) {
            return new File(info.publicSourceDir);
        }
        return null;
    }

    private List<FileRow> buildFileRows(File dir) {
        File[] files = dir.listFiles(mFilter); // 目录列表
        if (files != null) {
            sort(files); // 排序
            ArrayList<FileRow> fileRows = new ArrayList<FileRow>();
            boolean localShare = GlobalInit.getInstance().getLocalShare();
            if (!localShare) {
                // 显示/sdcard卡中的文件
                for (File file : files) {
                    fileRows.add(buildFileRow(file, false));
                }
                // 显示设备中已经安装的应用
                if (Config.SHOW_INSTALLED_APP) {
                    File appFiles[] = getFileFromDataApp();
                    sort(appFiles);
                    for (File file : appFiles) {
                        if (file != null/* && file.length() >= 1024 * 1024*/) {
                            fileRows.add(buildFileRow(file, true));
                        }
                    }
                }
            }
            // 获取本应用
            File seflFile = getSelfAppFile();
            if (seflFile != null) {
                Log.d(Log.TAG, "seflFile = " + seflFile);
                fileRows.add(0, buildFileRow(seflFile, true));
            }
            sortList(fileRows);
            return fileRows;
        }
        return null;
    }

    private SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd ahh:mm");

    private FileRow buildFileRow(File f, boolean installed) {
        boolean isDir = f.isDirectory();
        String clazz, name, link, size, icon = null, desc = null;
        long numSize = 0;
        if (isDir) {
            clazz = "icon dir";
            name = f.getName() + "/";
            link = f.getPath() + "/";
            size = "";
            numSize = 0;
        } else {
            clazz = "icon file";
            name = f.getName();
            link = f.getPath();
            if (name != null && name.endsWith(".apk")) {
                String label = getApkName(link);
                if (label != null) {
                    name = label;
                }
                icon = getApkIcon(link);
            }
            size = mCommonUtil.readableFileSize(f.length());
            numSize = f.length();
        }
        // desc = installed ? "Installed" : "UnInstalled";
        desc = link;
        FileRow row = new FileRow(clazz, name, link, size, icon, desc, numSize);
        row.time = sdf.format(new Date(f.lastModified()));
        if (f.canRead()) {
            row.can_browse = true;
            if (Config.ALLOW_DOWNLOAD && f.isFile()) {
                row.can_download = true;
            }
            if (f.canWrite()) {
                if (Config.ALLOW_DELETE) {
                    row.can_delete = true;
                }
                if (Config.ALLOW_UPLOAD && isDir) {
                    row.can_upload = true;
                }
            }
        }
        return row;
    }

    private ApkInfo getApkInfo(String apkFile) {
        if (TextUtils.isEmpty(apkFile)) {
            return null;
        }
        Context context = GlobalInit.getInstance().getBaseContext();
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return null;
        }
        ApkInfo apkInfo = new ApkInfo();
        PackageInfo info = pm.getPackageArchiveInfo(apkFile,
                PackageManager.GET_ACTIVITIES);
        ApplicationInfo applicationInfo = null;
        if (info != null) {
            applicationInfo = info.applicationInfo;
            if (applicationInfo != null) {
                applicationInfo.publicSourceDir = apkFile;
                CharSequence label = applicationInfo.loadLabel(pm);
                if (label != null) {
                    apkInfo.apkLabel = label.toString();
                }
                int index = apkFile.lastIndexOf("/");
                if (index != -1) {
                    apkInfo.apkName = apkFile.substring(index + 1);
                }
                apkInfo.packageName = applicationInfo.packageName;
                String ext = MimeTypeMap.getFileExtensionFromUrl(apkFile);
                apkInfo.apkDisName = apkInfo.apkLabel + "." + ext;
                apkInfo.downloadTime = System.currentTimeMillis();
            }
        }
        return apkInfo;
    }
    private String getApkName(String apkFile) {
        Context context = GlobalInit.getInstance().getBaseContext();
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return null;
        }
        PackageInfo info = pm.getPackageArchiveInfo(apkFile,
                PackageManager.GET_ACTIVITIES);
        ApplicationInfo appInfo = null;
        if (info != null) {
            appInfo = info.applicationInfo;
            if (appInfo != null) {
                appInfo.publicSourceDir = apkFile;
                CharSequence label = appInfo.loadLabel(pm);
                if (label != null) {
                    return label.toString();
                }
            }
        }
        return null;
    }

    private String getApkIcon(String apkFile) {
        Context context = GlobalInit.getInstance().getBaseContext();
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return null;
        }
        PackageInfo info = pm.getPackageArchiveInfo(apkFile,
                PackageManager.GET_ACTIVITIES);
        ApplicationInfo appInfo = null;
        if (info != null) {
            appInfo = info.applicationInfo;
            if (appInfo != null) {
                appInfo.publicSourceDir = apkFile;
                try {
                    Drawable drawable = appInfo.loadIcon(pm);
                    Bitmap bmp = null;
                    if (drawable instanceof BitmapDrawable) {
                        bmp = ((BitmapDrawable) drawable).getBitmap();
                    } else {
                        // TODO : 缺少默认图标
                        // bmp =
                        // BitmapFactory.decodeResource(context.getResources(),
                        // R.drawable.icon_default);
                    }
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte iconbyte[] = baos.toByteArray();
                    return Base64.encodeToString(iconbyte, Base64.DEFAULT);
                } catch (Exception e) {
                    Log.d(Log.TAG, "e = " + e);
                }
            }
        }
        return null;
    }

    /** 排序：文件夹、文件，再各安字符顺序 */
    private void sort(File[] files) {
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                if (f1.isDirectory() && !f2.isDirectory()) {
                    return -1;
                } else if (!f1.isDirectory() && f2.isDirectory()) {
                    return 1;
                } else {
                    return f1.toString().compareToIgnoreCase(f2.toString());
                }
            }
        });
    }

    // According to file size
    private void sortList(ArrayList<FileRow> fileRows) {
        Collections.sort(fileRows, new Comparator<FileRow>() {
            @Override
            public int compare(FileRow lhs, FileRow rhs) {
                return lhs.numSize < rhs.numSize ? 1 : -1;
            }
        });
    }

    private FileFilter mFilter = new FileFilter() {

        @Override
        public boolean accept(File pathname) {
            if (pathname.getName().endsWith(".apk")) {
                return true;
            }
            return false;
        }
    };

    private void printRequest(HttpRequest request) {
        Header headers[] = request.getAllHeaders();
        for (Header h : headers) {
            Log.d(Log.TAG, h.getName() + " : " + h.getValue());
        }
    }

    private void printResponse(HttpResponse res) {
        Header headers[] = res.getAllHeaders();
        for (Header h : headers) {
            Log.d(Log.TAG, h.getName() + " : " + h.getValue());
        }
    }

    private void redirectToDownload(String target, String hostAddress, HttpResponse response) {
        downloadStatistics(target);
        String ip = mCommonUtil.getLocalIpAddress();
        int port = Config.PORT;
        String localHost = ip + ":" + port;
        if (hostAddress != null && hostAddress.contains("127.0.0.1")) {
            ip = "http://127.0.0.1";
        }
        response.setStatusCode(302);
        if (!TextUtils.isEmpty(ip)) {
            ip = ip + ":" + port;
        } else {
            ip = "http://10.0.0.115:" + port;
        }
        if (!ip.startsWith("http")) {
            ip = "http://" + ip;
        }
        ip += TAG_APPFILE;
        ip += target;
        Log.d(Log.TAG, "Redirect ipaddress : " + ip);
        response.addHeader("Location", ip);
        response.addHeader("Expires", new Date().toGMTString());
        response.addHeader("Cache-Control", "1");
        Progress.clear();
    }

    private void downloadStatistics(String target) {
        Log.d(Log.TAG, "**********************************************************************target = " + target);
        final Context context = GlobalInit.getInstance().getBaseContext();
        final ApkInfo info = getApkInfo(target);
        Handler handler = new Handler(context.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                String text = info != null ? info.toString() : "";
                Toast.makeText(context, text, Toast.LENGTH_LONG).show();
            }
        });
        //Log.d(Log.TAG, "\n" + info.toString());
        String json = info.toJson();
        Log.d(Log.TAG, "\n" + json);
        Log.d(Log.TAG, "\n" + ApkInfo.fromJson(json));
    }

    private void redirectToView(HttpRequest request, HttpResponse response,
            String hostAddress, String requestMethod, Header requestHost) throws HttpException, IOException{
        String ip = mCommonUtil.getLocalIpAddress();
        int port = Config.PORT;
        String localHost = ip + ":" + port;
        if (hostAddress != null && hostAddress.contains("127.0.0.1")) {
            ip = "http://127.0.0.1";
        }
        Log.d(Log.TAG, "localHost = " + (localHost) + " , requestMethod = "
                + requestMethod);
        if ((localHost != null && localHost.equals(requestHost))
                || (requestMethod != null && requestMethod
                        .equalsIgnoreCase("POST"))) {
            Log.d(Log.TAG, "HttpStatus.SC_FORBIDDEN : " + ip);
            response.setStatusCode(HttpStatus.SC_FORBIDDEN);
            response.setEntity(resp403(request));
        } else {
            response.setStatusCode(301);
            if (!TextUtils.isEmpty(ip)) {
                ip = ip + ":" + port;
            } else {
                ip = "http://10.0.0.115:" + port;
            }
            if (!ip.startsWith("http")) {
                ip = "http://" + ip;
            }
            ip += TAG_APPLIST;
            Log.d(Log.TAG, "Redirect ipaddress : " + ip);
            response.addHeader("Location", ip);
        }
        Progress.clear();
    }
}

class ApkInfo {
    String  apkLabel;
    String  apkName;
    String  apkDisName;
    String  packageName;
    long    downloadTime;

    public String toString() {
        String str = "";
        str += "apkDisName   : " + apkDisName + "\n";
        str += "packageName  : " + packageName + "\n";
        str += "apkName      : " + apkName + "\n";
        str += "downloadTime : " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(downloadTime)) + "\n";
        return str;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static ApkInfo fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, ApkInfo.class);
    }
}
