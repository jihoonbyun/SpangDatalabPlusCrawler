package DatalabPlus;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class UpdateGrowthObject {

    private ArrayList<ProductInfo> array_list = new ArrayList<ProductInfo>();
    private double min_growth = -99999;
    public int array_num = 100;
    private int current_id = 0;

    UpdateGrowthObject(int array_num) {
        this.array_num = array_num;
    }

    public class ProductInfo  {

        private String product_no;
        private double growth;
        public ProductInfo(String product_no, double growth){
            this.product_no = product_no;
            this.growth = growth;
        }
        public double getGrowth(){
            return growth;
        }
        public String getProductNo() {
            return product_no;
        }

    }

    public int getArrayListLength() { return array_list.size();}

    public ArrayList<ProductInfo> getArrayList(){
        return array_list;
    }

    public void setId(int id) {
        this.current_id = id;
    }
    public int getId(){
        return current_id;
    }
    public void updateId(){
        this.current_id++;
    }


    public void updateProductNosAndGrowthList(JSONObject data) {
        if ((Double) data.get("월평균성장률12개월") > min_growth) {
            if (array_list.size() < array_num) {
                array_list.add(new ProductInfo((String) data.get("상품번호"),(Double) data.get("월평균성장률12개월")));
                Collections.sort(array_list, new Comparator<ProductInfo>() {
                    @Override
                    public int compare(ProductInfo o1, ProductInfo o2) {
                        return Double.compare(o2.getGrowth(), o1.getGrowth());
                    }
                });
                min_growth = array_list.get(array_list.size()-1).getGrowth();
            } else {
                for (int j = 0; j < array_list.size(); j++) {
                    if ((Double) data.get("월평균성장률12개월") > array_list.get(j).getGrowth()) {
                        if (array_list.size() == array_num) {
                            array_list.remove(array_num - 1);
                        }
                        array_list.add(j, new ProductInfo((String) data.get("상품번호"),(Double) data.get("월평균성장률12개월")));
                        min_growth = array_list.get(array_list.size()-1).getGrowth();;
                        break;
                    }
                }
            }
            System.out.println("쓰레드명 : " + Thread.currentThread().getName() + " 리스트 최신화");

        }
    }


}
