package Util;

public class StringMatching {

        //너무 느림
        int lcs( char[] X, char[] Y, int m, int n )
        {
            if (m == 0 || n == 0)
                return 0;
            if (X[m-1] == Y[n-1])
                return 1 + lcs(X, Y, m-1, n-1);
            else
                return max(lcs(X, Y, m, n-1), lcs(X, Y, m-1, n));
        }

        int max(int a, int b)
        {
            return (a > b)? a : b;
        }

        public int doLCS(String s1, String s2)
        {

            char[] X=s1.toCharArray();
            char[] Y=s2.toCharArray();
            int m = X.length;
            int n = Y.length;

            return lcs( X, Y, m, n );
        }



    // 잘안맞음
    public static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }

    //잘맞음
    public int wordMatching(String s1, String s2) {
            int hit = 0;
            String[] s1_group = s1.split(" ");
            String[] s2_group = s2.split(" ");

            for(int i =0; i < s1_group.length; i++) {
                for(int j=0; j < s2_group.length; j++) {
                    if(s1_group[i].equals(s2_group[j])) {
                        hit++;
                    }
                }
        }
            return hit;
    }

}
