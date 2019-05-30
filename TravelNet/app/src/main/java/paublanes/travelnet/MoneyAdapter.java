package paublanes.travelnet;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

public class MoneyAdapter extends RecyclerView.Adapter<MoneyAdapter.ViewHolder> {

    private ArrayList<MoneyInfo> moneyInfos;

    MoneyFunctionalities i_implementator;
    public interface MoneyFunctionalities {
        void deleteMoneyCategory(int index);
        void editMoneyCategory(int index);
    }

    public MoneyAdapter (Context context, ArrayList<MoneyInfo> list) {
        moneyInfos = list;
        i_implementator = (MoneyFunctionalities) context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tv_category, tv_amount;
        ImageButton btn_money_edit, btn_money_delete;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_category = itemView.findViewById(R.id.tv_category);
            tv_amount = itemView.findViewById(R.id.tv_amount);

            btn_money_edit = itemView.findViewById(R.id.btn_money_edit);
            btn_money_delete = itemView.findViewById(R.id.btn_money_delete);
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
        viewHolder.btn_money_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i_implementator.deleteMoneyCategory(i);
            }
        });
        viewHolder.btn_money_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i_implementator.editMoneyCategory(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return moneyInfos.size();
    }
}
