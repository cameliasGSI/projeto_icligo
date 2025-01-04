export interface ICar {
  id?: number;
  model?: string | null;
  brand?: string | null;
  cyear?: number | null;
  available?: boolean | null;
}

export const defaultValue: Readonly<ICar> = {
  available: false,
};
