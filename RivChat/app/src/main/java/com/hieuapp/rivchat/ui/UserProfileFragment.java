package com.hieuapp.rivchat.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hieuapp.rivchat.MainActivity;
import com.hieuapp.rivchat.R;
import com.hieuapp.rivchat.model.Configuration;
import com.hieuapp.rivchat.model.User;
import com.hieuapp.rivchat.util.ImageUtils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hieuttc on 05/12/2016.
 */

/**
 * Firebase: bhrivchat@gmail.com
 * Mk 123456a@
 */
public class UserProfileFragment extends Fragment {
    TextView tvUserName;
    ImageView avatar;

    private List<Configuration> listConfig = new ArrayList<>();
    private RecyclerView recyclerView;
    private UserInfoAdapter infoAdapter;

    private static final String USERNAME_LABEL = "Username";
    private static final String EMAIL_LABEL = "Email";
    private static final String SIGNOUT_LABEL = "Sign out";

    private static final int PICK_IMAGE = 1994;

    private DatabaseReference userDB;
    private User myAccount;

    private Context context;



    public UserProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userDB = FirebaseDatabase.getInstance().getReference().child("user").child(MainActivity.UID);

        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Lấy thông tin của user về và cập nhật lên giao diện
                listConfig.clear();
                myAccount = dataSnapshot.getValue(User.class);

                Configuration userNameConfig = new Configuration(USERNAME_LABEL, myAccount.name, R.mipmap.ic_account_box);
                listConfig.add(userNameConfig);

                Configuration emailConfig = new Configuration(EMAIL_LABEL, myAccount.email, R.mipmap.ic_email);
                listConfig.add(emailConfig);

                Configuration signout = new Configuration(SIGNOUT_LABEL, "", R.mipmap.ic_power_settings);
                listConfig.add(signout);

                if(infoAdapter != null){
                    infoAdapter.notifyDataSetChanged();
                }

                if(tvUserName != null){
                    tvUserName.setText(myAccount.name);
                }

                setImageAvatar(context, myAccount.avata);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Có lỗi xảy ra, không lấy đc dữ liệu
                Log.e(UserProfileFragment.class.getName(), "loadPost:onCancelled", databaseError.toException());
            }
        };
        userDB.addValueEventListener(userListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_info, container, false);
        context = view.getContext();
        avatar = (ImageView) view.findViewById(R.id.img_avatar);
        avatar.setOnClickListener(onAvatarClick);
        tvUserName = (TextView)view.findViewById(R.id.tv_username);

        initEmptyUser();

        if(myAccount == null){
            myAccount = new User();
            myAccount.avata = "default";
        }

        setImageAvatar(context, myAccount.avata);

        recyclerView = (RecyclerView)view.findViewById(R.id.info_recycler_view);
        infoAdapter = new UserInfoAdapter(listConfig);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(infoAdapter);

        initEmptyUser();

        return view;
    }

    private View.OnClickListener onAvatarClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Toast.makeText(context, "Có lỗi xảy ra, vui lòng thử lại", Toast.LENGTH_LONG).show();
                return;
            }
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(data.getData());
                Bitmap imgBitmap = BitmapFactory.decodeStream(inputStream);
                imgBitmap = ImageUtils.cropToSquare(imgBitmap);
                String imageBase64 = ImageUtils.encodeBase64(imgBitmap);
                userDB.child("avata").setValue(imageBase64);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void initEmptyUser(){
        listConfig.clear();

        Configuration userNameConfig = new Configuration(USERNAME_LABEL, "", R.mipmap.ic_account_box);
        listConfig.add(userNameConfig);

        Configuration emailConfig = new Configuration(EMAIL_LABEL, "", R.mipmap.ic_email);
        listConfig.add(emailConfig);

        Configuration signout = new Configuration(SIGNOUT_LABEL, "", R.mipmap.ic_power_settings);
        listConfig.add(signout);
    }

    private void setImageAvatar(Context context, String imgBase64){
        Resources res = getResources();
        //Nếu chưa có avatar thì để hình mặc định
        Bitmap src;
        if(imgBase64.equals("default")){
            src = BitmapFactory.decodeResource(res, R.drawable.user_default);
        }else {
            byte[] decodedString = Base64.decode(imgBase64, Base64.DEFAULT);
            src = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        }

        avatar.setImageDrawable(ImageUtils.roundedImage(context, src));
    }

    @Override
    public void onDestroyView (){
        super.onDestroyView();
    }

    @Override
    public void onDestroy (){
        super.onDestroy();
    }

    public class UserInfoAdapter extends RecyclerView.Adapter<UserInfoAdapter.ViewHolder>{
        private List<Configuration> profileConfig;

        public UserInfoAdapter(List<Configuration> profileConfig){
            this.profileConfig = profileConfig;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_info_item_layout, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Configuration config = profileConfig.get(position);
            holder.label.setText(config.getLabel());
            holder.value.setText(config.getValue());
            holder.icon.setImageResource(config.getIcon());
        }

        @Override
        public int getItemCount() {
            return profileConfig.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView label, value;
            public ImageView icon;
            public ViewHolder(View view) {
                super(view);
                label = (TextView)view.findViewById(R.id.tv_title);
                value = (TextView)view.findViewById(R.id.tv_detail);
                icon = (ImageView)view.findViewById(R.id.img_icon);
            }
        }

    }

}
