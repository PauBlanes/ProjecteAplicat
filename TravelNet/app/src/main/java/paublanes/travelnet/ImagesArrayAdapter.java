package paublanes.travelnet;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ImagesArrayAdapter extends RecyclerView.Adapter<ImagesArrayAdapter.ImageViewHolder> {

    private ArrayList<String> imagesList;
    private Context mContext;

    ImgGridFuncionalities i_implementator;
    public interface ImgGridFuncionalities {
        void onImgTap(int index, View view);
        void onImgLongTap(int index, View view);
    }

    public ImagesArrayAdapter (Context context, ArrayList<String> list) {
        this.imagesList = list;
        this.mContext = context;
        i_implementator = (ImgGridFuncionalities) context;
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageView iv_image;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            iv_image = itemView.findViewById(R.id.iv_images_item);
        }
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.images_list_item, viewGroup, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder viewHolder, int i) {

        Picasso.get()
                .load(imagesList.get(i))
                .fit()
                .centerCrop()
                .into(viewHolder.iv_image);

        viewHolder.iv_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i_implementator.onImgTap(i, viewHolder.iv_image);
            }
        });

        viewHolder.iv_image.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                i_implementator.onImgLongTap(i, v);
                return true;
            }
        });


    }

    @Override
    public int getItemCount() {
        return imagesList.size();
    }
}
