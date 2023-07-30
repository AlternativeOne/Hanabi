package com.lexoff.animediary.Extractor;

import com.lexoff.animediary.Client;
import com.lexoff.animediary.Info.Info;
import com.lexoff.animediary.Exception.ServiceUnavailableException;

import java.io.IOException;

import okhttp3.Response;

public abstract class Extractor {
    protected String BASE_URL="https://myanimelist.net/";

    protected String url;
    protected String data;

    private int method=0;
    private boolean throwOnErrorCodes=true;

    protected Client client;
    protected boolean pageFetched = false;

    protected String response;

    protected Extractor(Client client, String url){
        this.client=client;
        this.url=url;
    }

    public void setUrl(String url){
        this.url=url;
    }

    public void setData(String data){
        this.data=data;
    }

    public void setGET(){
        method=0;
    }

    public void setPOST(){
        method=1;
    }

    public void setThrowOnErrorCodes(boolean throwOnErrorCodes){
        this.throwOnErrorCodes=throwOnErrorCodes;
    }

    public void fetchPage() throws IOException, ServiceUnavailableException {
        if (pageFetched) return;
        Response response=null;
        if (method==0) {
            response=client.get(url);
        } else if (method==1){
            response=client.post(url, data);
        }

        if (throwOnErrorCodes && (response==null || response.code()!=200)) {
            throw new ServiceUnavailableException(response != null
                    ? String.format("Service responded with: code=%d", response.code())
                    : "Call's answer is empty");
        }

        onPageFetched(response);
        pageFetched = true;
    }

    protected void assertPageFetched() {
        if (!pageFetched) throw new IllegalStateException("Page is not fetched.");
    }

    protected boolean isPageFetched() {
        return pageFetched;
    }

    public void onPageFetched(Response response) throws IOException {
        String body=response.body().string();

        this.response=body;
    }

    public Info getInfo() throws IOException, ServiceUnavailableException {
        fetchPage();

        return buildInfo();
    }

    protected abstract Info buildInfo();
}
