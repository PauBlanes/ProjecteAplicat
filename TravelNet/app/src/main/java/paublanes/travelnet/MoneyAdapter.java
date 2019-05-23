package paublanes.travelnet;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class MoneyAdapter extends RecyclerView.Adapter<MoneyAdapter.ViewHolder> {

    private ArrayList<MoneyInfo> moneyInfos;

    MoneyFunctionalities i_implementator;
    public interface MoneyFunctionalities {
        void moneyDeleteMenu(View itemView, int index);
    }

    public MoneyAdapter (Context context, ArrayList<MoneyInfo> list) {
        moneyInfos = list;
        i_implementator = (MoneyFunctionalities) context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tv_category, tv_amount;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_category = itemView.findViewById(R.id.tv_category);
            tv_amount = itemView.findViewById(R.id.tv_amount);
        }
    }

    @NonNull
    @Override
    public MoneyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.money_row_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoneyAdapter.ViewHolder viewHolder, int i) {
        viewHolder.tv_category.setText(moneyInfos.get(i).getCategory());
        viewHolder.tv_amount.setText(moneyInfos.get(i).getAmount() +  "$");
        i_implementator.moneyDeleteMenu(viewHolder.itemView, i);
    }

    @Override
    public int getItemCount() {
        return moneyInfos.size();
    }
}
