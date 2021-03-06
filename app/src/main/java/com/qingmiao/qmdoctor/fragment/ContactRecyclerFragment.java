package com.qingmiao.qmdoctor.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import com.github.jdsjlzx.interfaces.OnRefreshListener;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.github.jdsjlzx.recyclerview.ProgressStyle;
import com.qingmiao.qmdoctor.R;
import com.qingmiao.qmdoctor.adapter.ContactAdapter;
import com.qingmiao.qmdoctor.bean.ContactModel;
import com.qingmiao.qmdoctor.utils.PrefUtils;
import com.qingmiao.qmdoctor.widget.CharacterParser;
import com.qingmiao.qmdoctor.widget.PatientDividerDecoration;
import com.qingmiao.qmdoctor.widget.PinyinComparator;
import com.qingmiao.qmdoctor.widget.SideBar;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ContactRecyclerFragment extends BaseFragment {

    public String did;
    public String token;
    @BindView(R.id.contact_list)
    LRecyclerView contactList;
    @BindView(R.id.sidrbar)
    SideBar sideBar;
    @BindView(R.id.dialog)
    TextView dialog;
    LRecyclerViewAdapter mLRecyclerViewAdapter;
    private ContactAdapter adapter;
    private View headerView, footerView;
    private OnRefreshListener listener;
    private boolean isShowSideBar;
    private boolean isShowCheckBox;
    private boolean isOpenSwipeButton;
    private  OnContactItemClickListener onItemClickListener;
    private int REQUEST_COUNT = 10;
    private LinearLayoutManager layoutManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = View.inflate(getContext(), R.layout.fragment_contact, null);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void initView() {
        did = PrefUtils.getString(getActivity(), "did", "");
        token = PrefUtils.getString(getActivity(), "token", "");
        sideBar.setTextView(dialog);
        WindowManager wm = getActivity().getWindowManager();
        Display d = wm.getDefaultDisplay();
        //拿到布局参数
        ViewGroup.LayoutParams l = sideBar.getLayoutParams();
        l.height=d.getHeight()/2;
        // 设置右侧[A-Z]快速导航栏触摸监听
        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                // 该字母首次出现的位置
                layoutManager.scrollToPositionWithOffset(adapter.getScrollPosition(s), 0);
            }
        });
        if (isShowSideBar) {
            sideBar.setVisibility(View.VISIBLE);
        } else {
            sideBar.setVisibility(View.GONE);
        }
    }

    private void initRecycleView() {
        List<ContactModel> contactModels = new ArrayList<>();
        layoutManager = new LinearLayoutManager(this.getContext());
        contactList.setLayoutManager(layoutManager);
        contactList.setItemAnimator(new DefaultItemAnimator());
        PatientDividerDecoration divider = new PatientDividerDecoration.Builder(this.getContext())
                .setColorResource(R.color.text)
                .build();
        contactList.addItemDecoration(divider);

        adapter = new ContactAdapter(this.getContext(), contactModels);
        adapter.setShowChecker(isShowCheckBox);
        adapter.setOpenSwipeButton(isOpenSwipeButton);
        LRecyclerViewAdapter mLRecyclerViewAdapter = new LRecyclerViewAdapter(adapter);
        contactList.setAdapter(mLRecyclerViewAdapter);
        contactList.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);
        contactList.setArrowImageView(R.drawable.ic_pulltorefresh_arrow);
        if (headerView != null) {
            mLRecyclerViewAdapter.addHeaderView(headerView);
        } else {
          //  mLRecyclerViewAdapter.removeHeaderView();
        }
        if (footerView != null) {
            mLRecyclerViewAdapter.addFooterView(footerView);
        } else {
            mLRecyclerViewAdapter.removeFooterView();
        }
        if(listener != null){
            contactList.setPullRefreshEnabled(true);
            contactList.setOnRefreshListener(listener);
        }else{
            contactList.setPullRefreshEnabled(false);
        }
        if(onItemClickListener!=null){
            adapter.setOnItemClickListener(onItemClickListener);
        }
    }


    public LRecyclerView  getRecycleView(){
        return contactList;
    }


    public void setRefreshComplete(){
        // 停止刷新
        contactList.refreshComplete(REQUEST_COUNT);
    }



    public void setOnItemClickListener(OnContactItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }



    public void addHeaderView(View view) {
        this.headerView = view;
    }

    public void setRefreshListener(OnRefreshListener listener) {
        this.listener = listener;
    }

    public void addFooterView(View view) {
        this.footerView = view;
    }

    public void setOpenSwipeButton(boolean openSwipeButton) {
        isOpenSwipeButton = openSwipeButton;
    }


    public void setShowSideBar(boolean isShow) {
        this.isShowSideBar = isShow;
    }

    public void setShowCheckBox(boolean showCheckBox) {
        isShowCheckBox = showCheckBox;
    }

    @Override
    public void initData() {
        initDatas();
        initRecycleView();
    }

    private void initDatas() {
    }

    public void upDataList(List<ContactModel> contactModels) {
        adapter.updateListView(contactModels);
    }

    public void setAllData(List<ContactModel> contactModels){
        adapter.setmAllDatas(contactModels);
    }

    public ContactAdapter getAdapter() {
        return adapter;
    }

    public List<ContactModel> getDataList(){
        return adapter.getDataList();
    }


    public List<ContactModel> getCheckData() {
        return adapter.getCheckModel();
    }


    public List<ContactModel> filterData(String s) {
        List<ContactModel> tempExaple = new ArrayList<>();
        if(TextUtils.isEmpty(s)){
            upDataList(adapter.getmAllDatas());
            return adapter.getmAllDatas();
        }else{
            CharacterParser characterParser = new CharacterParser();
            tempExaple.clear();
            for(ContactModel contactModel : getAdapter().getmAllDatas()){
                String name = contactModel.friend.getShowName();
                if(name!=null && (name.indexOf(s.toString()) != -1 || characterParser.getSelling(name).startsWith(s.toString()))){
                    tempExaple.add(contactModel);
                }
            }
            Collections.sort(tempExaple, new PinyinComparator());
            upDataList(tempExaple);
            return tempExaple;
        }
    }


    public interface OnContactItemClickListener{
        public void onItemClick(View view, int position,ContactModel model);
    }

}
