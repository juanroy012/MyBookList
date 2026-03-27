const OL_BASE = "https://openlibrary.org";

export interface OLSearchResult {
  key: string;
  title: string;
  author_name?: string[];
  cover_i?: number;
  first_publish_year?: number;
  isbn?: string[];
  first_sentence?: string[];
  number_of_pages_median?: number;
  subject?: string[];
  ratings_average?: number;
  ratings_count?: number;
  edition_count?: number;
}

export interface OLSearchResponse {
  numFound: number;
  docs: OLSearchResult[];
}

export interface OLWorkDetail {
  title: string;
  description?: string | { value: string };
  covers?: number[];
  subjects?: string[];
  first_sentence?: { value: string };
  key: string;
  authors?: Array<{ author?: { key?: string } }>;
  created?: { value?: string };
  first_publish_date?: string;
  publishers?: string[];
  identifiers?: { isbn_10?: string[]; isbn_13?: string[] };
}

export interface OLTrendingResult {
  key: string;
  title: string;
  cover_id?: number;
  cover_edition_key?: string;
  author_name?: string[];
  first_publish_year?: number;
}

export interface OLTrendingResponse {
  works: OLTrendingResult[];
}

export const openLibrary = {
  search: async (query: string, page = 1, limit = 20): Promise<OLSearchResponse> => {
    const res = await fetch(
      `${OL_BASE}/search.json?q=${encodeURIComponent(query)}&page=${page}&limit=${limit}&fields=key,title,author_name,cover_i,first_publish_year,isbn,first_sentence,number_of_pages_median,subject,ratings_average,ratings_count,edition_count`
    );
    return res.json();
  },

  getWork: async (workId: string): Promise<OLWorkDetail> => {
    const res = await fetch(`${OL_BASE}/works/${workId}.json`);
    return res.json();
  },

  getTrending: async (type: "daily" | "weekly" | "monthly" = "weekly"): Promise<OLTrendingResponse> => {
    const res = await fetch(`${OL_BASE}/trending/${type}.json?limit=12`);
    return res.json();
  },

  coverUrl: (coverId: number | undefined, size: "S" | "M" | "L" = "M"): string | null => {
    if (!coverId) return null;
    return `https://covers.openlibrary.org/b/id/${coverId}-${size}.jpg`;
  },

  workIdFromKey: (key: string): string => {
    // key looks like "/works/OL12345W"
    return key.replace("/works/", "");
  },
};
