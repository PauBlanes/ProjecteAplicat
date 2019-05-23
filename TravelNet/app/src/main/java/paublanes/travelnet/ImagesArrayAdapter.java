package paublanes.travelnet;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ImagesArrayAdapter extends RecyclerView.Adapter<ImagesArrayAdapter.ImageViewHolder> {

    private ArrayList<String> imagesList;
    private Context mContext;

    ImgGridFuncionalities i_implementator;
    public interface ImgGridFuncionalities {
        void imgDeleteMenu(View itemView, int index);
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

        i_implementator.imgDeleteMenu(viewHolder.itemView, i);
    }

    @Override
    public int getItemCount() {
        return imagesList.size();
    }
}
