package com.ispw.progettoispw.bean;
import java.util.Objects;





    public class BarbiereBean{
        private final String id;
        private final String displayName; // es. "Mario Rossi"
        private final String email;       // opzionale, utile per debug/tooltip

        public BarbiereBean(String id, String displayName, String email) {
            this.id = id;
            this.displayName = displayName;
            this.email = email;
        }

        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public String getEmail() { return email; }

        @Override public String toString() { return displayName == null ? "" : displayName; }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BarbiereBean that)) return false;
            return Objects.equals(id, that.id);
        }

    }


