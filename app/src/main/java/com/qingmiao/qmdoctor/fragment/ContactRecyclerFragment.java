package com.qingmiao.qmdoctor.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.github.jdsjlzx.ItemDecoration.DividerDecoration;
import com.github.jdsjlzx.interfaces.OnItemClickListener;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.github.jdsjlzx.recyclerview.ProgressStyle;
import com.qingmiao.qmdoctor.R;
import com.qingmiao.qmdoctor.adapter.ContactAdapter;
import com.qingmiao.qmdoctor.bean.ContactModel;
import com.qingmiao.qmdoctor.bean.PatientFriendListBean;
import com.qingmiao.qmdoctor.bean.UserFriendBean;
import com.qingmiao.qmdoctor.global.KeyOrValueGlobal;
import com.qingmiao.qmdoctor.utils.PrefUtils;
import com.qingmiao.qmdoctor.widget.CharacterParser;
import com.qingmiao.qmdoctor.widget.PinyinComparator;
import com.qingmiao.qmdoctor.widget.SideBar;
import java.util.ArrayList;
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
    private LinearLayoutManager layoutManager;
    private ContactAdapter adapter;
    private View headerView, footerView;
    private boolean isShowSideBar;
    private boolean isShowCheckBox;
    private boolean isOpenSwipeButton;
    private  OnContactItemClickListener onItemClickListener;


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
        adapter = new ContactAdapter(this.getContext(), contactModels);
        adapter.setShowChecker(isShowCheckBox);
        adapter.setOpenSwipeButton(isOpenSwipeButton);
        contactList.setLayoutManager(layoutManager);
        DividerDecoration divider = new DividerDecoration.Builder(this.getContext())
                .setColorResource(R.color.text)
                .build();
        contactList.addItemDecoration(divider);
        mLRecyclerViewAdapter = new LRecyclerViewAdapter(adapter);
        contactList.setAdapter(mLRecyclerViewAdapter);
        contactList.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);
        contactList.setArrowImageView(R.drawable.ic_pulltorefresh_arrow);

        if (headerView != null) {
            mLRecyclerViewAdapter.addHeaderView(headerView);
        } else {
            mLRecyclerViewAdapter.removeHeaderView();
        }
        if (footerView != null) {
            mLRecyclerViewAdapter.addFooterView(footerView);
        } else {
            mLRecyclerViewAdapter.removeFooterView();
        }
        contactList.setPullRefreshEnabled(false);
        if(onItemClickListener!=null){
            adapter.setOnItemClickListener(onItemClickListener);
        }
    }

    public void setOnItemClickListener(OnContactItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public LRecyclerViewAdapter getmLRecyclerViewAdapter() {
        return mLRecyclerViewAdapter;
    }

    public void addHeaderView(View view) {
        this.headerView = view;
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
            for (int i = 0; i < adapter.getDataList().size(); i++) {
                ContactModel contactModel = adapter.getDataList().get(i);
                if (contactModel.type == ContactAdapter.ITEM_TYPE.ITEM_TYPE_CONTACT.ordinal() && contactModel.friend != null) {
                    if (contactModel.friend.nickname.contains(s)) {
                        tempExaple.add(contactModel);
                    }
                }
            }
            upDataList(tempExaple);
            return tempExaple;
        }
    }


    public interface OnContactItemClickListener{
        public void onItemClick(View view, int position,ContactModel model);
    }

}
